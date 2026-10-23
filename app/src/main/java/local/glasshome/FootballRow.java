package local.glasshome;

import android.app.AlertDialog;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/** Visible-only fixture panel backed by explicitly offered iCalendar subscriptions. */
final class FootballRow extends LinearLayout {
 private final MainActivity home;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private final ExecutorService worker=Executors.newSingleThreadExecutor();
 private final TextView[] tabs=new TextView[2];
 private final ArrayList<View> cards=new ArrayList<>();
 private final LinearLayout list;
 private final TextView source;
 private final ArrayList<FootballFixtures.Match> matches=new ArrayList<>();
 private boolean active=false; private int generation=0,selected=0,available=0,fresh=0;
 private Future<?> pending;
 private static final long TTL=6*60*60*1000L,RETRY=15*60*1000L;
 private final Runnable midnight=()->{render();startLoad();scheduleMidnight();};

 FootballRow(MainActivity h){
  super(h);home=h;setOrientation(VERTICAL);setPadding(h.dp(12),h.dp(8),h.dp(12),h.dp(6));setBackground(h.surface(20,false,0x243c4863));setClipChildren(false);
  LinearLayout bar=new LinearLayout(h);bar.setGravity(Gravity.CENTER_VERTICAL);addView(bar,new LayoutParams(-1,h.dp(24)));
  TextView heading=h.text("Fútbol",13,0xffedf3ff);bar.addView(heading,new LayoutParams(0,-2,1));
  for(int i=0;i<2;i++){
   final int index=i;TextView tab=h.text(i==0?"Hoy":"Mañana",11,0xffd0d9ef);tab.setGravity(Gravity.CENTER);tab.setFocusable(true);tab.setClickable(true);tab.setPadding(h.dp(9),0,h.dp(9),0);tabs[i]=tab;bar.addView(tab,new LayoutParams(-2,h.dp(24)));
   tab.setOnClickListener(v->{selected=index;render();});
   tab.setOnFocusChangeListener((v,f)->{if(f){selected=index;render();h.glass.setVisibility(INVISIBLE);h.scroll.smoothScrollTo(0,0);}else styleTabs();});
   tab.setOnKeyListener((v,k,e)->{if(e.getAction()!=KeyEvent.ACTION_DOWN)return false;if(k==19)return true;if(k==20){if(!cards.isEmpty())cards.get(0).requestFocus();else dock();return true;}if(k==21){if(index>0)tabs[index-1].requestFocus();return true;}if(k==22){if(index==0)tabs[1].requestFocus();else h.top.getChildAt(0).requestFocus();return true;}return false;});
  }
  ScrollView scroll=new ScrollView(h);scroll.setVerticalScrollBarEnabled(false);scroll.setFocusable(false);scroll.setClipToPadding(false);LayoutParams sp=new LayoutParams(-1,0,1);sp.topMargin=h.dp(3);addView(scroll,sp);
  list=new LinearLayout(h);list.setOrientation(VERTICAL);scroll.addView(list,new ScrollView.LayoutParams(-1,-2));
  source=h.text("Cargando calendarios…",8,0xffaebed9);source.setPadding(0,h.dp(3),0,0);addView(source,new LayoutParams(-1,h.dp(15)));
  render();
 }
 boolean focusTab(){tabs[selected].requestFocus();return true;}
 private void left(){tabs[selected].requestFocus();}
 private void dock(){if(home.watch.focusCard(home.lastDock))return;if(!home.dockTiles.isEmpty())home.dockTiles.get(Math.min(home.lastDock,home.dockTiles.size()-1)).requestFocus();}
 private void styleTabs(){for(int i=0;i<2;i++){boolean f=tabs[i].hasFocus();tabs[i].setBackground(home.surface(12,f,i==selected?0x705574a1:0x103c4863));tabs[i].setTextColor(i==selected?0xffffffff:0xffaebed9);}}
 void start(){active=true;render();startLoad();scheduleMidnight();}
 void stop(){active=false;generation++;handler.removeCallbacks(midnight);if(pending!=null)pending.cancel(true);}
 void destroy(){stop();worker.shutdownNow();}
 private void scheduleMidnight(){handler.removeCallbacks(midnight);long wait=Duration.between(Instant.now(),LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()).toMillis();if(active)handler.postDelayed(midnight,Math.max(1000,wait+1000));}
 private File file(int i){return new File(home.getFilesDir(),"football-"+FootballFixtures.SLUGS[i]+".ics");}
 private void startLoad(){
  if(!active)return;final int gen=++generation;
  pending=worker.submit(()->{
   ArrayList<FootballFixtures.Match> all=new ArrayList<>();int loaded=0,current=0;
   // First show disk cache without waiting for the network.
   for(int i=0;i<5;i++)try{File f=file(i);if(f.isFile()){all.addAll(FootballFixtures.parse(readFile(f),i));loaded++;if(System.currentTimeMillis()-f.lastModified()<TTL)current++;}}catch(Exception ignored){}
   publish(all,loaded,current,gen);
   for(int i=0;i<5;i++){
    if(Thread.currentThread().isInterrupted())return;File f=file(i);long now=System.currentTimeMillis();
    if(f.isFile()&&now-f.lastModified()<TTL)continue;
    String pref="football-attempt-"+i;long attempt=home.getPreferences(0).getLong(pref,0);if(now-attempt<RETRY)continue;
    home.getPreferences(0).edit().putLong(pref,now).apply();
    try{String body=download("https://ics.fixtur.es/v2/"+FootballFixtures.SLUGS[i]+".ics");FootballFixtures.parse(body,i);
     android.util.AtomicFile atomic=new android.util.AtomicFile(f);FileOutputStream out=null;
     try{out=atomic.startWrite();out.write(body.getBytes(StandardCharsets.UTF_8));atomic.finishWrite(out);}catch(IOException ex){if(out!=null)atomic.failWrite(out);throw ex;}
    }catch(Exception ex){android.util.Log.w("GlassFootball","Calendar unavailable: "+FootballFixtures.SLUGS[i]);}
   }
   if(Thread.currentThread().isInterrupted())return;all.clear();loaded=0;current=0;
   for(int i=0;i<5;i++)try{File f=file(i);if(f.isFile()){all.addAll(FootballFixtures.parse(readFile(f),i));loaded++;if(System.currentTimeMillis()-f.lastModified()<TTL)current++;}}catch(Exception ignored){}
   publish(all,loaded,current,gen);
  });
 }
 private void publish(ArrayList<FootballFixtures.Match> all,int loaded,int current,int gen){ArrayList<FootballFixtures.Match> snapshot=new ArrayList<>(all);handler.post(()->{if(!active||gen!=generation)return;matches.clear();matches.addAll(snapshot);available=loaded;fresh=current;render();});}
 private static String readFile(File f)throws IOException{try(InputStream in=new FileInputStream(f)){return read(in);}}
 private static String read(InputStream in)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int count;while((count=in.read(buffer))!=-1){if(out.size()+count>2*1024*1024)throw new IOException("Calendar too large");out.write(buffer,0,count);}return out.toString(StandardCharsets.UTF_8.name());}
 private static String download(String link)throws IOException{
  HttpURLConnection c=(HttpURLConnection)new URL(link).openConnection();c.setConnectTimeout(5000);c.setReadTimeout(5000);c.setInstanceFollowRedirects(false);c.setRequestProperty("Accept","text/calendar");c.setRequestProperty("User-Agent","GlassHome/1.3 (personal calendar subscription)");
  try{if(c.getResponseCode()!=200)throw new IOException("Calendar HTTP "+c.getResponseCode());try(InputStream in=c.getInputStream()){return read(in);}}finally{c.disconnect();}
 }
 private void render(){
  styleTabs();View focused=home.getCurrentFocus();int focusIndex=cards.indexOf(focused);list.removeAllViews();cards.clear();
  ZoneId zone=ZoneId.systemDefault();LocalDate day=LocalDate.now(zone).plusDays(selected);List<FootballFixtures.Match> today=FootballFixtures.forDay(matches,day,zone);
  if(today.isEmpty()){
   TextView empty=home.text(available<5?"Calendarios pendientes de actualizar":"Sin partidos "+(selected==0?"hoy":"mañana"),10,0xffd3ddef);empty.setPadding(0,home.dp(3),0,home.dp(3));list.addView(empty);
   TextView teams=home.text("Real Madrid · Bayern · Barcelona\nAtlético Nacional · Colombia",9,0xffaebed9);list.addView(teams);
  }
  for(int i=0;i<today.size();i++){
   FootballFixtures.Match m=today.get(i);final int index=i;LinearLayout card=new LinearLayout(home);card.setGravity(Gravity.CENTER_VERTICAL);card.setPadding(home.dp(5),home.dp(3),home.dp(5),home.dp(3));card.setFocusable(true);card.setClickable(true);card.setBackground(home.surface(12,false,0x183c4863));LayoutParams cp=new LayoutParams(-1,home.dp(44));cp.bottomMargin=home.dp(3);list.addView(card,cp);cards.add(card);
   int res=home.getResources().getIdentifier("football_"+m.team,"drawable",home.getPackageName());if(res!=0){ImageView badge=new ImageView(home);badge.setImageResource(res);badge.setScaleType(ImageView.ScaleType.FIT_CENTER);LayoutParams bp=new LayoutParams(home.dp(25),home.dp(25));bp.rightMargin=home.dp(8);card.addView(badge,bp);}
   LinearLayout labels=new LinearLayout(home);labels.setOrientation(VERTICAL);card.addView(labels,new LayoutParams(0,-2,1));TextView title=home.text(m.title,10,0xfff0f4ff);title.setMaxLines(2);title.setEllipsize(android.text.TextUtils.TruncateAt.END);labels.addView(title);
   String hour=m.kickoff==null?"Horario por confirmar":DateTimeFormatter.ofPattern("HH:mm").withZone(zone).format(m.kickoff);TextView time=home.text(hour+(m.competition.isEmpty()?"":" · "+m.competition),9,0xffb0c5ea);time.setSingleLine(true);time.setEllipsize(android.text.TextUtils.TruncateAt.END);labels.addView(time);
   card.setContentDescription(m.title+", "+hour);
   card.setOnFocusChangeListener((v,f)->{v.setBackground(home.surface(12,f,0x183c4863));if(f){home.glass.setVisibility(INVISIBLE);home.scroll.smoothScrollTo(0,0);}});
   card.setOnClickListener(v->new AlertDialog.Builder(home).setTitle(m.title).setMessage((selected==0?"Hoy":"Mañana")+" · "+hour+"\n"+zone.getId()+(m.competition.isEmpty()?"":"\n"+m.competition)+"\n\nCalendario: Fixtur.es\nLos horarios pueden cambiar. No incluye transmisión ni marcador en directo.").setPositiveButton("Cerrar",null).show());
   card.setOnKeyListener((v,k,e)->{if(e.getAction()!=KeyEvent.ACTION_DOWN)return false;if(k==19){if(index==0)tabs[selected].requestFocus();else cards.get(index-1).requestFocus();return true;}if(k==20){if(index+1<cards.size())cards.get(index+1).requestFocus();else dock();return true;}if(k==21){left();return true;}if(k==22){home.top.getChildAt(0).requestFocus();return true;}return false;});
  }
  source.setText("Fixtur.es · "+(available<5?available+"/5 calendarios":fresh<5?"Datos guardados; actualización pendiente":"Hora local · Sin directo"));
  if(focusIndex>=0){if(cards.isEmpty())tabs[selected].requestFocus();else cards.get(Math.min(focusIndex,cards.size()-1)).requestFocus();}
 }
}
