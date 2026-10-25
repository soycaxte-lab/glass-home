param(
 [string]$Device,
 [string]$Adb = 'adb',
 [string]$Apk = (Join-Path $PSScriptRoot '../downloads/GlassHome-1.5.0.apk'),
 [string]$ChecksumFile = (Join-Path $PSScriptRoot '../downloads/SHA256SUMS'),
 [switch]$SetHome
)
$ErrorActionPreference = 'Stop'
function Invoke-Device([string[]]$Arguments) {
 $output = & $Adb -s $Device @Arguments 2>&1
 if ($LASTEXITCODE -ne 0) { throw ($output -join "`n") }
 return ($output -join "`n")
}
if (!(Test-Path -LiteralPath $Apk)) { throw "APK no encontrado: $Apk" }
if (!(Test-Path -LiteralPath $ChecksumFile)) { throw "No se encuentra el archivo SHA256SUMS: $ChecksumFile" }
$apkName = Split-Path -Leaf $Apk
$expected = @([System.IO.File]::ReadAllLines((Resolve-Path -LiteralPath $ChecksumFile).Path) | ForEach-Object {
 if ($_ -match '^([0-9a-fA-F]{64})\s+\*?(.+)$' -and $Matches[2].Trim() -ceq $apkName) { $Matches[1].ToLowerInvariant() }
})
if ($expected.Count -ne 1) { throw "SHA256SUMS debe contener una sola suma para $apkName." }
$actual = (Get-FileHash -LiteralPath $Apk -Algorithm SHA256).Hash.ToLowerInvariant()
if ($actual -cne $expected[0]) { throw 'El SHA-256 no coincide. No se instalará el APK.' }
Write-Host "SHA-256 verificado: $apkName"
if ([string]::IsNullOrWhiteSpace($Device)) { $Device = Read-Host 'IP:puerto de conexión de Wireless debugging (no el de emparejamiento)' }
if ([string]::IsNullOrWhiteSpace($Device) -or $Device -match '\s') { throw 'Indica una dirección de conexión válida.' }

& $Adb connect $Device
if ($LASTEXITCODE -ne 0) { throw 'No se pudo conectar por ADB.' }
$state = Invoke-Device -Arguments @('get-state')
if ($state.Trim() -ne 'device') { throw 'El dispositivo no está autorizado o disponible.' }
$deviceUser = (Invoke-Device -Arguments @('shell','am','get-current-user')).Trim()
if ($deviceUser -notmatch '^\d+$') { throw 'No se pudo identificar el usuario Android activo.' }
$resolved = Invoke-Device -Arguments @('shell','cmd','package','resolve-activity','--user',$deviceUser,'--brief','-a','android.intent.action.MAIN','-c','android.intent.category.HOME')
$component = @($resolved -split "`n" | Where-Object { $_.Trim() -match '^[a-zA-Z0-9_.]+/[a-zA-Z0-9_.$]+$' } | ForEach-Object { $_.Trim() }) | Select-Object -Last 1
$stamp = Get-Date -Format 'yyyyMMdd-HHmmss-fff'
$folder = Join-Path $PSScriptRoot "../backups/$stamp"
New-Item -ItemType Directory -Path $folder -Force | Out-Null
@{device=$Device;user=$deviceUser;home=$component;created=$stamp;apkSha256=$actual} | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $folder 'home.json') -Encoding UTF8
try { $paths = Invoke-Device -Arguments @('shell','pm','path','local.glasshome') } catch { $paths = '' }
foreach ($line in ($paths -split "`n")) {
 if ($line.Trim() -match '^package:(.+/base\.apk)$') {
  Invoke-Device -Arguments @('pull',$Matches[1],(Join-Path $folder 'GlassHome-before.apk')) | Write-Host
 }
}
$result = Invoke-Device -Arguments @('install','-r',(Resolve-Path -LiteralPath $Apk).Path)
Write-Host $result
if ($result -notmatch '(?m)^Success\s*$') { throw 'Android no confirmó la instalación.' }
try { Invoke-Device -Arguments @('shell','pm','grant','--user',$deviceUser,'local.glasshome','android.permission.READ_TV_LISTINGS') | Write-Host }
catch { Write-Warning 'No se pudo conceder acceso a los listados de TV. Continue Watching puede no estar disponible.' }
if ($SetHome) {
 Invoke-Device -Arguments @('shell','cmd','package','set-home-activity','--user',$deviceUser,'local.glasshome/.MainActivity') | Write-Host
 Invoke-Device -Arguments @('shell','input','keyevent','KEYCODE_HOME') | Write-Host
 Write-Host 'Comprueba con el control físico que Home abre Glass Home.'
} else {
 Invoke-Device -Arguments @('shell','am','start','-n','local.glasshome/.MainActivity') | Write-Host
}
Write-Host "Respaldo local: $folder"
Write-Host 'Prueba el launcher antes de elegirlo como inicio. Al terminar, apaga y revoca la depuración.'
