# Instalar y volver al inicio anterior

## Antes de instalar

Usa un equipo Android TV/Google TV y conserva acceso a Ajustes con el control físico. Anota qué launcher abre HOME. No hace falta root, borrar datos ni desinstalar aplicaciones del sistema.

Activa las opciones de desarrollador y la depuración que permita tu TV. Los nombres de menús dependen del fabricante. La dirección de conexión inalámbrica puede cambiar después de reiniciar. No uses la dirección de conexión como puerto de emparejamiento: son pantallas y puertos distintos.

```sh
adb pair IP:PUERTO_DE_EMPAREJAMIENTO
adb connect IP:PUERTO_DE_CONEXION
adb devices
```

`adb pair` solo se necesita si el dispositivo ofrece ese mecanismo. Introduce su código temporal en tu propia terminal; no lo publiques.

## Instalar y probar

Descarga el APK y comprueba su SHA-256 con `sha256sum` o `Get-FileHash` frente a `downloads/SHA256SUMS`. Luego instala:

```sh
adb -s IP:PUERTO install -r GlassHome-1.6.0.apk
adb -s IP:PUERTO shell pm grant local.glasshome android.permission.READ_TV_LISTINGS
adb -s IP:PUERTO shell am start -n local.glasshome/.MainActivity
```

Si el permiso no está disponible en tu firmware, las otras funciones pueden seguir usándose; Continue Watching mostrará su limitación. Si hay un error de firma al actualizar, conserva los datos y comprueba el origen del APK. No desinstales para eludirlo sin entender que podrías perder la configuración de esa app.

En Windows, desde la carpeta del repositorio:

```powershell
.\scripts\install.ps1 -Device IP:PUERTO
```

El script crea una carpeta local `backups/` ignorada por Git. Guarda el HOME resuelto antes de instalar y el APK anterior de Glass Home si existía. No respalda los datos privados de las apps ni las claves del sistema.

Prueba el control remoto, Wi-Fi/Ajustes, el dock, la cuadrícula y la reproducción en tus servicios. Para Continue Watching, primero reproduce un título en una app que publique Watch Next, vuelve a Home y prueba su tarjeta. Cada app controla su perfil y la reanudación final.

## Seleccionar HOME

Si tu sistema permite elegir la aplicación de inicio en Ajustes, selecciona Glass Home allí. Como alternativa, después de probarlo:

```sh
adb -s IP:PUERTO shell cmd package set-home-activity --user 0 local.glasshome/.MainActivity
adb -s IP:PUERTO shell input keyevent KEYCODE_HOME
```

O ejecuta el instalador de Windows con `-SetHome`. Comprueba el resultado real y reinicia una vez antes de darlo por terminado.

Algunos firmwares de Google TV interceptan HOME aunque el comando se acepte. Eso necesita una revisión de ese modelo; estos scripts no deshabilitan el launcher de fábrica ni asistentes de configuración. En el Xiaomi original un asistente era propietario de la red Wi-Fi guardada: deshabilitarlo sin revisar sus dependencias podía eliminar esa conexión. Esa intervención específica no forma parte de la instalación pública.

## Restaurar

Selecciona el launcher anterior en Ajustes. Si usaste el script y existe un respaldo válido:

```powershell
.\scripts\restore-home.ps1 -Device IP:PUERTO -BackupFile .\backups\CARPETA\home.json
```

La restauración utiliza el componente exacto guardado, sin asumir que todos los dispositivos tienen Google TV Home. Glass Home y sus datos permanecen instalados. Si el HOME guardado ya era Glass Home o no se pudo identificar, elige el launcher de fábrica desde Ajustes.

## Cerrar la depuración

Cuando todo funcione, revoca las autorizaciones de depuración, apaga Wireless/USB/Network/ADB debugging donde existan y reinicia. No dejes la depuración activada para usar el launcher; no la necesita después de instalarse.
