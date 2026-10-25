"""Read-only benchmark of shared runtime metrics; does not install APKs or change HOME."""
import subprocess,time,re,json,sys,statistics
from pathlib import Path
import argparse
parser=argparse.ArgumentParser(description="Measure the installed Glass Home; temporarily opens Settings and moves focus. Keep the remote idle.")
parser.add_argument("--adb",default="adb")
parser.add_argument("--device",required=True)
parser.add_argument("--label",required=True)
parser.add_argument("--output",default="backups/performance")
args=parser.parse_args();adb=args.adb;serial=args.device;phase=args.label
if not re.fullmatch(r"[a-zA-Z0-9_-]+",phase):raise ValueError("Use letters, digits, underscores or hyphens for label")
output=Path(args.output);output.mkdir(parents=True,exist_ok=True)
def run(*args):return subprocess.check_output([adb,"-s",serial,*args],text=True,encoding="utf8",errors="replace",stderr=subprocess.STDOUT)
if run("get-state").strip()!="device":raise RuntimeError("ADB device not ready")
component="local.glasshome/.MainActivity"
run("shell","am","start","-W","-n",component);time.sleep(3)
out={"phase":phase,"cold_wait_ms":[],"return_wait_ms":[],"jank":[],"model":run("shell","getprop","ro.product.model").strip()}
for mode in ("cold","return"):
 for i in range(8):
  if mode=="cold":run("shell","am","force-stop","local.glasshome")
  else:run("shell","am","start","-W","-a","android.settings.SETTINGS")
  launch=run("shell","am","start","-W","-n",component)
  out[mode+"_wait_ms"].append(int(re.search(r"WaitTime:\s*(\d+)",launch).group(1)));time.sleep(.8)
for sample in range(3):
 run("shell","input","keyevent","4");run("shell","input","keyevent",*(["21"]*8));time.sleep(2)
 run("shell","dumpsys","gfxinfo","local.glasshome","reset")
 for i in range(10):
  for key in ("22","22","21","21"):run("shell","input","keyevent",key);time.sleep(.08)
 time.sleep(1)
 gfx=run("shell","dumpsys","gfxinfo","local.glasshome")
 (output/(phase+"-gfx-"+str(sample)+".txt")).write_text(gfx,encoding="utf8")
 out["jank"].append({"frames":int(re.search(r"Total frames rendered:\s*(\d+)",gfx).group(1)),"janky":int(re.search(r"Janky frames:\s*(\d+)",gfx).group(1)),"p50_ms":int(re.search(r"50th percentile:\s*(\d+)",gfx).group(1)),"p95_ms":int(re.search(r"95th percentile:\s*(\d+)",gfx).group(1))})
(output/(phase+"-meminfo.txt")).write_text(run("shell","dumpsys","meminfo","local.glasshome"),encoding="utf8")
(output/(phase+".json")).write_text(json.dumps(out,indent=2),encoding="utf8")
print(json.dumps(out),flush=True)
