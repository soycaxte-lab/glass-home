# Prompt completo en español para instalar Glass Home

Copia **todo el bloque** con el botón de copiar y pégalo en un asistente de programación con acceso a archivos, terminal y red. El asistente necesitará tu ayuda para activar la depuración y probar el control físico. No pegues contraseñas ni códigos de emparejamiento en un chat público.

Este prompt reproduce la distribución **1.3.2-compact** y el proceso del Xiaomi original, adaptando los pasos del sistema al dispositivo de cada persona. La apariencia se conserva usando el APK del proyecto; las apps, tarjetas y partidos dependen de tu equipo y de sus proveedores. No garantiza compatibilidad con todos los firmwares.

```text
Quiero que instales y configures Glass Home en mi Android TV o Google TV siguiendo este proceso completo. Trabaja en español y lleva la instalación hasta verificarla en la TV. Puedes descargar el proyecto, inspeccionar el dispositivo, guardar respaldos e instalar el launcher. Pídeme únicamente los datos y las acciones físicas que realmente necesites. Explica de forma breve los avances y cualquier limitación real.

PROYECTO Y ARCHIVOS OFICIALES

- Repositorio: https://github.com/soycaxte-lab/glass-home
- APK de esta distribución: https://github.com/soycaxte-lab/glass-home/raw/refs/heads/main/downloads/GlassHome-1.3.2.apk
- Sumas SHA-256: https://github.com/soycaxte-lab/glass-home/blob/main/downloads/SHA256SUMS
- Instalación y restauración: https://github.com/soycaxte-lab/glass-home/blob/main/docs/INSTALACION.md
- Compilación: https://github.com/soycaxte-lab/glass-home/blob/main/docs/DESARROLLO.md
- Código descargable: https://github.com/soycaxte-lab/glass-home/archive/refs/heads/main.zip

Lee primero README.md, docs/INSTALACION.md, docs/DESARROLLO.md y los scripts del repositorio. Descarga la versión indicada y compara su SHA-256 con el archivo de sumas de la misma revisión del repositorio. Si falta el archivo, no coincide la suma o la documentación cambió materialmente, detente y explica el problema. Registra la revisión y la suma utilizadas para que el resultado sea reproducible. No descargues APK de otras páginas ni inventes archivos que no existan.

1. OBJETIVO: CONSERVAR ESTA INTERFAZ

Usa el APK oficial existente, sin rediseñarlo ni reemplazarlo por otro launcher. El paquete es local.glasshome y la actividad es local.glasshome/.MainActivity. La versión de esta interfaz es 1.3.2-compact (versionCode 6); el archivo distribuido se llama GlassHome-1.3.2.apk.

El resultado visual debe conservar:
- Fondo oscuro azul/violeta con luces curvas y paneles de cristal translúcido, bordes iluminados y degradados.
- Efectos de enfoque y escala suaves al moverse con el control remoto. Fondo estático para evitar carga continua de desenfoque o vídeo.
- Hora y fecha a la izquierda; Wi-Fi y Ajustes a la derecha.
- Widget de fútbol compacto de 216 x 104 dp centrado arriba entre el reloj y los controles. No lo ensanches: ya se redujo a la mitad para despejar la vista.
- Sin botón Inputs/Entradas en esta interfaz.
- Fila Continuar viendo debajo del encabezado.
- Dock flotante de cuatro aplicaciones en este orden: Netflix, TEVEO, Prime Video y YouTube.
- Cuadrícula de aplicaciones accesible con la flecha abajo.
- Fuente Outfit incluida con su licencia.

Las aplicaciones de streaming se instalan por separado y requieren las cuentas del usuario. No contienen suscripciones ni contenido en este repositorio. Si falta alguna, informa y permite instalarla desde su fuente oficial; no uses versiones modificadas. Los iconos proceden de las apps instaladas.

El launcher requiere Android 8.0 o superior. La referencia física original es un Xiaomi MiTV-AFMU0 con Android 14, pantalla 1920 x 1080. Inspecciona el modelo real, versión, resolución y densidad antes de hacer cambios. No cambies resolución o densidad para forzar la apariencia. Samsung Tizen y LG webOS no ejecutan este APK; si ese es mi equipo, explica que hace falta un dispositivo externo Android TV/Google TV conectado por HDMI.

2. CONEXIÓN Y RESPALDOS

Primero averigua qué herramientas tienes disponibles en el ordenador. Usa Android Platform Tools de Google para ADB y elige siempre el dispositivo explícitamente, sin actuar sobre otros teléfonos o televisores conectados.

Pídeme marca/modelo y la dirección completa IP:PUERTO mostrada ahora en Wireless debugging. No asumas ninguna IP de otra instalación ni que el puerto sigue igual después de reiniciar. Confirma si la conexión de red es Wi-Fi o Ethernet y si hay adaptadores o dispositivos USB que deban conservarse.

Si aparece emparejamiento inalámbrico, distingue su puerto del puerto de conexión:
  adb pair IP:PUERTO_DE_EMPAREJAMIENTO
  adb connect IP:PUERTO_DE_CONEXION
  adb devices

Permite que introduzca el código temporal directamente en mi terminal. No guardes ni publiques códigos o contraseñas. Si no hay conexión, comprueba el endpoint actual y el estado de autorización antes de proponer otros métodos.

Antes de instalar o cambiar HOME, guarda localmente:
- Modelo, versión Android y estado de conexión.
- Componente HOME original resuelto y lista de launchers disponibles.
- Paquetes relevantes habilitados/deshabilitados y valores DNS originales si se fueran a cambiar.
- APK existente de Glass Home y su versión/firma, si ya está instalado.
- Lista exacta de cualquier cambio que hagas y cómo revertirlo.

Los scripts públicos guardan el HOME anterior y el APK de Glass Home si existe; no son una copia completa del dispositivo ni respaldan los datos privados de otras apps. Si HOME ya era Glass Home, no lo presentes como un respaldo del launcher de fábrica. Guarda los respaldos fuera de cualquier carpeta que se vaya a publicar.

No hagas root, restablecimiento de fábrica, borrado de datos, desinstalación de apps del sistema ni cambios de cuentas. Conserva voz, control remoto, Bluetooth, HDMI-CEC, Wi-Fi/Ethernet, Google Play Services, actualizaciones y funciones de reproducción. Conserva Chromecast/Miracast cuando existan, salvo que yo solicite otra cosa.

3. INSTALAR Y PROBAR ANTES DE CAMBIAR HOME

Prefiere el APK oficial verificado para obtener la misma interfaz. No recompiles si no es necesario.

En Windows puedes usar desde la carpeta del repositorio:
  .\scripts\install.ps1 -Device IP:PUERTO

Lee el script antes de ejecutarlo. Este instalador no deshabilita paquetes del sistema ni cambia DNS. Alternativamente, tras hacer los respaldos:
  adb -s IP:PUERTO install -r GlassHome-1.3.2.apk
  adb -s IP:PUERTO shell pm grant local.glasshome android.permission.READ_TV_LISTINGS
  adb -s IP:PUERTO shell am start -n local.glasshome/.MainActivity

Comprueba los resultados de cada comando. Si READ_TV_LISTINGS no está disponible en ese firmware, explica la limitación de Continuar viendo y prueba el resto. No declares que el permiso se concedió si falló.

Si la instalación da error de firma, conserva la app y sus datos, inspecciona la firma instalada y la del APK y explícame las opciones. No desinstales automáticamente para saltar el conflicto. Las compilaciones de depuración y los artefactos de CI pueden tener otra firma.

Si solicito personalización del código, usa el código existente y la guía de desarrollo: Java 17, SDK 35 y el wrapper Gradle incluido. No reconstruyas una interfaz aproximada desde cero. Las claves privadas de la distribución no están en el repositorio; una compilación propia necesita su propia firma y puede no actualizar directamente la instalación oficial. No publiques claves ni contraseñas de firma.

Abre el launcher sin cambiar todavía el HOME predeterminado. Inspecciona la pantalla real, cuando tengas acceso autorizado a capturas, y pídeme probar fluidez y legibilidad con el control físico. No uses la ilustración del README como evidencia de una prueba real.

4. CONTINUAR VIENDO

Usa la implementación Watch Next incluida. Lee los listados compartidos de Android TV; no busques historiales en bases privadas, no pidas credenciales de streaming ni extraigas sesiones.

Para poblar la fila, pídeme reproducir un título en una app compatible y volver al launcher. Comprueba que aparece y que al pulsarlo abre la app correcta y reanuda el título. Netflix y Prime Video funcionaron en el dispositivo original, pero la disponibilidad depende de versión, perfil y dispositivo. Puede aparecer primero el selector de perfil: deja que yo elija y confirme la reanudación.

TEVEO es una app independiente. NO modifiques, descompiles, parches, firmes ni sustituyas su APK. NO instales una versión antigua para recuperar tarjetas. TEVEO solo aparece si su propia versión publica registros Watch Next compatibles; si no los publica, explica que su desarrollador debe integrar esa función. No prometas que el launcher puede recuperar el historial privado. Conserva esta regla al actualizar TEVEO u otras apps.

La fila utiliza títulos con progreso publicados como continuaciones, y los enlaces se limitan a la app que publicó la tarjeta. Si está vacía, distingue falta de permiso, falta de registros y falta de compatibilidad. No crees tarjetas falsas para disimularlo.

5. FÚTBOL DE HOY Y MAÑANA

Conserva el widget compacto incluido, con estos equipos iniciales:
- Real Madrid.
- Bayern Múnich.
- FC Barcelona.
- Atlético Nacional.
- Selección de Colombia, no un club llamado Colombia FC.

Usa las suscripciones iCalendar gratuitas de Fixtur.es implementadas en el proyecto. Los identificadores son real-madrid, bayern-munchen, fc-barcelona, atletico-nacional y co. No extraigas periódicamente datos de FotMob ni solicites una API de pago para esta configuración.

Muestra Hoy/Mañana según la zona horaria de la TV, encuentros como local y visitante cuando estén publicados, sin duplicar partidos entre favoritos. No inventes un horario cuando el calendario no lo especifica. Respeta la caché de seis horas y los reintentos de al menos 15 minutos; no conviertas el panel en un servicio continuo. No es un marcador en directo y la cobertura depende de la fuente. Si no hay encuentros publicados para esos días, muestra el estado vacío de forma honesta.

Comprueba ambas pestañas, la hora local y el detalle de un partido cuando haya alguno. No fuerces un partido de otra fecha para que parezca que el widget funciona. Si necesito cambiar equipos en esta versión, revisa la configuración del código y explica si requiere compilar: no inventes una pantalla de selección que no existe.

6. SELECCIONAR GLASS HOME COMO INICIO

Solo después de comprobar el funcionamiento, selecciona Glass Home como aplicación de inicio desde Ajustes cuando el firmware lo permita. Como alternativa:
  adb -s IP:PUERTO shell cmd package set-home-activity --user 0 local.glasshome/.MainActivity
  adb -s IP:PUERTO shell input keyevent KEYCODE_HOME

En Windows el instalador admite -SetHome. Comprueba el resultado físico; un comando aceptado no garantiza que el firmware deje de interceptar HOME.

Si HOME sigue abriendo el launcher de fábrica, inspecciona específicamente ese modelo y sus dependencias. No ejecutes una lista genérica de paquetes a deshabilitar. Conserva acceso a Ajustes y una ruta de restauración antes de cualquier intervención adicional. Si hace falta deshabilitar un componente de fábrica, explica cuál, por qué, qué puede afectar y obtén mi autorización específica para ese cambio.

En el Xiaomi original hubo una particularidad: com.google.android.tungsten.setupwraith era propietario de la red Wi-Fi guardada. Antes de deshabilitarlo hubo que olvidar y volver a conectar la red desde Ajustes, dejando que el usuario introdujera su contraseña, y comprobar que la red ya no dependía de ese paquete. Esto desconecta temporalmente ADB y puede cambiar el puerto. No repitas ese paso por rutina ni deshabilites el propietario de la red sin resolver la dependencia. Si corresponde a mi dispositivo, guíame, espera a que confirme la reconexión y solicita el nuevo endpoint antes de continuar.

Si no se puede cambiar HOME de forma reversible y comprobada, deja instalado el launcher y explica el impedimento; no sacrifiques la red o las funciones del control para forzarlo.

7. OPCIONES DEL PROCESO ORIGINAL: DNS Y LIMPIEZA

Estas opciones no son necesarias para conseguir la interfaz. No las apliques por defecto. Pregúntame si quiero DNS con bloqueo de anuncios y si quiero revisar paquetes de recomendaciones/telemetría. Continúa las otras tareas mientras respondo.

Si elijo DNS con bloqueo, la opción del equipo original fue DNS privado dns.adguard-dns.com. Guarda antes los valores exactos de private_dns_mode y private_dns_specifier, comprueba que el firmware admite DNS privado, aplica solo esa configuración autorizada y prueba conectividad, inicio de sesión y reproducción. No afirmes que bloquea publicidad de YouTube. Si falla la red o una app, restaura los valores originales; distingue valores ausentes de cadenas literales.

Como referencia histórica, en aquel Xiaomi se deshabilitaron estos seis paquetes:
  com.miui.tv.analytics
  com.xiaomi.statistic
  com.mitv.tvhome.oemtab
  com.mitv.tvhome.michannel
  com.google.android.apps.tv.launcherx
  com.google.android.tungsten.setupwraith

Esta lista NO es una receta universal ni una instrucción para deshabilitarlos todos. Los dos últimos están relacionados con HOME/configuración y requieren la revisión del apartado anterior. Verifica que cada paquete existe, su función, dependencias y estado previo en mi equipo. No deshabilites otros componentes por parecido de nombre. Conserva entradas y funciones de otros modelos cuando sean necesarias, aunque esta interfaz no muestre el botón Inputs.

Si autorizo un cambio concreto, usa una operación reversible para el usuario correcto, un paquete por vez, y comprueba el resultado antes del siguiente. Registra cómo devolverlo a su estado anterior; no habilites indiscriminadamente paquetes que ya estaban deshabilitados antes. No uses desinstalación del sistema ni borrado de datos como sustituto.

8. VALIDACIÓN COMPLETA CON EL CONTROL REAL

Comprueba conmigo:
- Flechas, OK, Atrás, HOME, acceso a Ajustes y navegación entre dock y cuadrícula.
- Dock en el orden Netflix → TEVEO → Prime Video → YouTube.
- Fondo, paneles de cristal, enfoque fluido y widget de fútbol pequeño centrado, sin Inputs.
- Continuar viendo: desplazamiento, app correcta y reanudación de títulos reales disponibles.
- Fútbol: Hoy/Mañana, detalle, estado vacío y navegación hacia Wi-Fi/Ajustes.
- Control por voz, botones de acceso directo y volumen, incluyendo pulsación sostenida de subir volumen si el control lo admite.
- Reproducción real en Netflix, TEVEO, Prime Video y YouTube instalados. No basta con abrir sus pantallas.
- Wi-Fi/Ethernet, Bluetooth y funciones que utilizo.

Como referencia de navegación: desde el dock, arriba entra en Continuar viendo y abajo en la cuadrícula. Desde Continuar viendo, arriba llega a Fútbol y abajo al dock. En Fútbol, izquierda/derecha cambia Hoy/Mañana; derecha desde Mañana va a Wi-Fi y después a Ajustes. Abajo entra a los partidos si existen o vuelve a Continuar viendo. Atrás regresa al dock.

Reinicia una vez después de los cambios. Pídeme confirmar que HOME sigue abriendo Glass Home y que el control y la reproducción siguen funcionando. Si necesitas volver a conectarte, solicita el puerto inalámbrico actual; no supongas que el anterior sobrevivió. Comprueba que los cambios autorizados persisten y conserva evidencia local sin publicar historial personal.

9. RESTAURACIÓN Y CIERRE

Entrega la ubicación de los respaldos, el resumen de cambios efectivos y los pasos de restauración específicos. Para volver al inicio anterior, usa Ajustes o el componente exacto guardado. En Windows, si existe un respaldo válido del script:
  .\scripts\restore-home.ps1 -Device IP:PUERTO -BackupFile .\backups\CARPETA\home.json

Si se deshabilitó un paquete autorizado, restaura también su estado anterior cuando corresponda antes de volver a depender de él. Para DNS restaura los valores guardados. No describas el respaldo del APK como respaldo de sus datos privados.

Cuando todo esté validado, guíame para revocar autorizaciones de depuración, desactivar Wireless/USB/Network/ADB debugging donde existan y reiniciar. El launcher no requiere depuración para funcionar. Si compruebas puertos desde el ordenador, distingue rechazo de conexión de un timeout; una prueba de un puerto no demuestra por sí sola que no exista ningún otro abierto.

No publiques mis IP, redes, contraseñas, códigos, cuentas, historiales, respaldos, configuraciones privadas o claves de firma. Si preparo una copia para compartir, incluye únicamente el proyecto público, licencias y documentación. Mantén TEVEO separado.

Al terminar, indica qué quedó instalado y comprobado, las funciones que dependan de terceros y cualquier prueba pendiente. No declares una instalación completa ni una prueba física exitosa sin haberla realizado y recibido mi confirmación cuando haga falta.
```
