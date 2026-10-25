$ErrorActionPreference = 'Stop'
$repository = Split-Path -Parent $PSScriptRoot
$testRoot = Join-Path $repository ('build/installer-check-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path (Join-Path $testRoot 'scripts'),(Join-Path $testRoot 'downloads') -Force | Out-Null
Copy-Item -LiteralPath (Join-Path $repository 'scripts/install.ps1') -Destination (Join-Path $testRoot 'scripts/install.ps1')
Copy-Item -LiteralPath (Join-Path $repository 'scripts/restore-home.ps1') -Destination (Join-Path $testRoot 'scripts/restore-home.ps1')
$testApk = Join-Path $testRoot 'downloads/GlassHome-1.5.0.apk'
[System.IO.File]::WriteAllText($testApk,'mock APK bytes; never installed on a device')
$manifest = Join-Path $testRoot 'downloads/SHA256SUMS'
$global:GlassHomeMockCalls = [System.Collections.Generic.List[string]]::new()
$global:GlassHomeFailInstall = $false
function global:GlassHomeMockAdb {
 $command = $args -join ' '
 $global:GlassHomeMockCalls.Add($command)
 $global:LASTEXITCODE = 0
 if ($command -match 'get-state$') { 'device' }
 elseif ($command -match 'get-current-user$') { '10' }
 elseif ($command -match 'resolve-activity') { 'com.example.home/.Home' }
 elseif ($command -match 'pm path') { '' }
 elseif ($command -match 'install -r') { if ($global:GlassHomeFailInstall) { $global:LASTEXITCODE=1; 'Failure [MOCK_ERROR]' } else { 'Success' } }
 else { 'OK' }
}
function Assert-Check($condition,$message) { if (!$condition) { throw $message } }
$install = Join-Path $testRoot 'scripts/install.ps1'
[System.IO.File]::WriteAllText($manifest,('0' * 64) + '  GlassHome-1.5.0.apk' + "`n")
$failed = $false
try { & $install -Device 'test.invalid:1234' -Adb GlassHomeMockAdb } catch { $failed = $true }
Assert-Check $failed 'Incorrect checksum accepted'
Assert-Check ($global:GlassHomeMockCalls.Count -eq 0) 'ADB called before verifying checksum'
$hash = (Get-FileHash -LiteralPath $testApk -Algorithm SHA256).Hash.ToLowerInvariant()
[System.IO.File]::WriteAllText($manifest,$hash + '  GlassHome-1.5.0.apk' + "`n")
& $install -Device 'test.invalid:1234' -Adb GlassHomeMockAdb -SetHome
Assert-Check (@($global:GlassHomeMockCalls | Where-Object { $_ -match 'set-home-activity --user 10' }).Count -eq 1) 'HOME used wrong user'
Assert-Check (@($global:GlassHomeMockCalls | Where-Object { $_ -match 'pm grant --user 10' }).Count -eq 1) 'Permission used wrong user'
$backups = @(Get-ChildItem -Path (Join-Path $testRoot 'backups/*/home.json'))
Assert-Check ($backups.Count -eq 1) 'Missing HOME backup'
$saved = Get-Content -LiteralPath $backups[0].FullName -Raw | ConvertFrom-Json
Assert-Check ($saved.user -eq '10' -and $saved.home -eq 'com.example.home/.Home' -and $saved.apkSha256 -eq $hash) 'Invalid backup fields'
$global:GlassHomeMockCalls.Clear();$global:GlassHomeFailInstall=$true;$failed=$false
try { & $install -Device 'test.invalid:1234' -Adb GlassHomeMockAdb -SetHome } catch { $failed=$true }
Assert-Check $failed 'Installation failure ignored'
Assert-Check (@($global:GlassHomeMockCalls | Where-Object { $_ -match 'set-home-activity' }).Count -eq 0) 'HOME changed after install failure'
$global:GlassHomeMockCalls.Clear();$saved.user='0';$wrongBackup=Join-Path $testRoot 'wrong-user.json';$saved | ConvertTo-Json | Set-Content -LiteralPath $wrongBackup
$failed=$false
try { & (Join-Path $testRoot 'scripts/restore-home.ps1') -Device 'test.invalid:1234' -Adb GlassHomeMockAdb -BackupFile $wrongBackup } catch { $failed=$true }
Assert-Check $failed 'Restoration for another Android user accepted'
Assert-Check (@($global:GlassHomeMockCalls | Where-Object { $_ -match 'set-home-activity' }).Count -eq 0) 'HOME changed for another user'
Remove-Item Function:\GlassHomeMockAdb
Write-Host 'PASS: 10 installer checksum, backup, user and failure-path checks; no device or network used.'
