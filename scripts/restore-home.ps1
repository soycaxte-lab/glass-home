param(
 [Parameter(Mandatory=$true)][string]$Device,
 [Parameter(Mandatory=$true)][string]$BackupFile,
 [string]$Adb = 'adb'
)
$ErrorActionPreference = 'Stop'
$saved = Get-Content -LiteralPath $BackupFile -Raw | ConvertFrom-Json
$component = [string]$saved.home
if ($component -notmatch '^[a-zA-Z0-9_.]+/[a-zA-Z0-9_.$]+$' -or $component.StartsWith('local.glasshome/')) {
 throw 'El respaldo no identifica otro launcher. Elige el inicio anterior desde Ajustes.'
}
if ($saved.device -ne $Device) { Write-Warning 'La dirección del respaldo es distinta. Comprueba que estás conectado a la misma TV; los puertos pueden cambiar.' }
& $Adb connect $Device
if ($LASTEXITCODE -ne 0) { throw 'No se pudo conectar.' }
$currentUser = (& $Adb -s $Device shell am get-current-user).Trim()
if ($LASTEXITCODE -ne 0 -or $currentUser -notmatch '^\d+$') { throw 'No se pudo identificar el usuario activo.' }
$restoreUser = if ($null -ne $saved.user) { [string]$saved.user } else { '0' }
if ($restoreUser -ne $currentUser) { throw 'El respaldo pertenece a otro usuario Android. Cambia a ese usuario antes de restaurar.' }
$result = & $Adb -s $Device shell cmd package set-home-activity --user $restoreUser $component 2>&1
Write-Host ($result -join "`n")
if ($LASTEXITCODE -ne 0 -or ($result -join "`n") -match '(?i)error|exception') { throw 'No se pudo restaurar HOME; usa Ajustes.' }
& $Adb -s $Device shell input keyevent KEYCODE_HOME
Write-Host 'Comprueba que aparece el launcher anterior. Glass Home y sus datos siguen instalados.'
