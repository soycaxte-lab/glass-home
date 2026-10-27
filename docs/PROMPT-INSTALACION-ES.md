# Prompt completo en español: instalación guiada de Glass Home 1.6.0

Copia todo el bloque y pégalo en un asistente con acceso autorizado a archivos, terminal e Internet en un ordenador de tu red local. Te preguntará los datos que falten y te guiará con los pasos físicos de la TV. Un chat sin esas herramientas puede explicarlo, pero no instalar por ti.

La versión **1.6.0-teams** añade un selector de equipos desde la TV y conserva el diseño de cristal y las optimizaciones del regreso a Home. El APK oficial garantiza la misma base visual; apps, continuaciones, recomendaciones y calendarios dependen de cada equipo. No hay una prueba que permita llamarlo «el más rápido de todos».

```text
Quiero instalar o actualizar Glass Home 1.6.0-teams en mi Android TV o Google TV. Trabaja en español y encárgate de preparar herramientas, verificar la descarga, respaldar lo necesario, instalar, probar y explicar la restauración. Guíame hasta terminar; pregúntame los datos, preferencias y pasos físicos que necesites. No te limites a darme instrucciones si tienes herramientas y autorización para ejecutarlas.

0. ARCHIVOS OFICIALES Y ALCANCE

Repositorio: https://github.com/soycaxte-lab/glass-home
Release: https://github.com/soycaxte-lab/glass-home/releases/tag/v1.6.0
APK: https://github.com/soycaxte-lab/glass-home/releases/download/v1.6.0/GlassHome-1.6.0.apk
SHA256SUMS: https://github.com/soycaxte-lab/glass-home/releases/download/v1.6.0/SHA256SUMS
Proyecto de esta versión: https://github.com/soycaxte-lab/glass-home/archive/refs/tags/v1.6.0.zip
Tutorial: https://github.com/soycaxte-lab/glass-home/blob/v1.6.0/docs/TUTORIAL-ES.md
Guía de instalación: https://github.com/soycaxte-lab/glass-home/blob/v1.6.0/docs/INSTALACION.md
Desarrollo: https://github.com/soycaxte-lab/glass-home/blob/v1.6.0/docs/DESARROLLO.md
Rendimiento: https://github.com/soycaxte-lab/glass-home/blob/v1.6.0/docs/RENDIMIENTO-1.5.0.md

Lee README, las guías y los scripts antes de ejecutarlos. Descarga solo de este repositorio y las herramientas de sus proveedores oficiales. Registra la revisión y la suma utilizadas. Si un archivo falta, una suma no coincide o una firma no es compatible, detente en esa operación y explica el motivo; no inventes enlaces ni desinstales para saltar errores.

Esta autorización incluye inspección por ADB, respaldos locales, instalación/actualización del launcher y pruebas. No incluye root, borrar datos, restablecer de fábrica, extraer cuentas o sesiones, deshabilitar paquetes del sistema ni modificar reproductores. Los cambios opcionales necesitan mi elección específica. Conserva la configuración existente cuando no solicite cambiarla.

1. ENTREVISTA INICIAL: PREGÚNTAME LO NECESARIO

Haz preguntas claras, en grupos pequeños de hasta tres. Ofrece los valores recomendados y no vuelvas a preguntar lo que ya respondí o puedas comprobar con herramientas. Continúa el trabajo independiente mientras respondo. No tomes el silencio como permiso.

Averigua:
- Ordenador y sistema operativo; si el asistente dispone de terminal y acceso a la red local. Inspecciona herramientas ya instaladas antes de pedir que las instale.
- Marca/modelo del Android TV o box y si es instalación nueva o actualización. Comprueba por ADB el Android, usuario activo, versión de Glass Home, resolución y densidad cuando conecte.
- Si están en la misma red local. Pídeme la IP:PUERTO de conexión que muestre ahora Wireless debugging, y si ofrece emparejamiento por código. No asumas direcciones ni puertos de otra persona.
- Si utiliza Wi-Fi o Ethernet, USB o adaptadores que deban conservarse. Conserva voz, Bluetooth, volumen, HDMI-CEC, actualizaciones y casting que utilice.
- Apps de streaming que uso. Recomienda el dock oficial Netflix → TEVEO → Prime Video → YouTube y confirma si quiero ese mismo diseño o una personalización.
- Fútbol: confirma si conservo Real Madrid, Bayern Múnich, Barcelona, Atlético Nacional y selección de Colombia. La distribución tiene un selector de 296 clubes y selecciones, con uno a cinco favoritos. Si prefiero otros, guíame en Fútbol → ⚙ → Mis equipos de fútbol, buscar, marcar y Guardar. No hace falta compilar para cambiar equipos.
- Si quiero recomendaciones editoriales públicas de películas/series y Continuar viendo. Explica sus fuentes y dependencias. No presentes estas preguntas como ajustes que ya existan: cambiar el dock o retirar paneles requiere modificar y compilar una variante, con firma propia y sus límites de actualización. Propón primero instalar el APK oficial sin modificar.
- Si quiero seleccionar Glass Home como HOME después de probarlo, o solo instalarlo para evaluarlo.

No pidas contraseñas de Wi-Fi ni de streaming en el chat: las introduzco directamente en sus pantallas. No hagas preguntas sobre DNS o limpieza como si fueran requisitos de la interfaz. Solo evalúalas si las solicito, por separado y con respaldo y pruebas de conectividad.

Si mi TV usa Samsung Tizen o LG webOS, este APK no funciona directamente: explica que necesito un dispositivo Android TV/Google TV por HDMI. Requiere Android 8.0 o superior; otros firmwares pueden limitar HOME o Watch Next.

2. INTERFAZ QUE DEBES CONSERVAR

Usa el APK oficial 1.6.0-teams (versionCode 13), paquete local.glasshome y actividad local.glasshome/.MainActivity. No reconstruyas una aproximación desde cero.
- Fondo estático azul/violeta con bandas amarillo perlado y menta, paneles de cristal y enfoque animado; sin vídeo ni desenfoque continuo.
- Saludo/pregunta a la izquierda, fecha/hora centradas, fútbol compacto de 224 x 92 dp justo debajo; Wi-Fi, balón y Ajustes a la derecha.
- Recomendación grande a la izquierda y Continuar viendo a la derecha con imágenes completas y ancho adaptable a su proporción.
- Explorar / Películas / Series, aplicadas con OK.
- Dock de cuatro iconos en el orden indicado y cuadrícula con flecha abajo; sin botón Inputs en este diseño.
- Fuente Outfit incluida con licencia.

La optimización reutiliza apps y vistas al regresar, conserva foco y actualiza progresos sin reconstruir todo. El catálogo de apps se invalida con instalaciones/actualizaciones/eliminaciones. No requiere dejar ADB encendido ni añadir servicios permanentes. No prometas tiempos de otro hardware ni que acelera el servidor de Netflix u otra app.

3. PREPARAR ADB, CONECTAR Y RESPALDAR

Usa Android Platform Tools de Google. Selecciona siempre el dispositivo explícitamente con -s; no actúes sobre otros teléfonos, emuladores o televisores conectados.

Cuando el firmware ofrezca emparejamiento:
  adb pair IP:PUERTO_DE_EMPAREJAMIENTO
Introduce el código temporal en mi propia terminal. Luego usa el puerto de conexión de la pantalla principal:
  adb connect IP:PUERTO_DE_CONEXION
  adb devices

No confundas los puertos. Si el endpoint cambia, pídeme el actual. No pruebes direcciones al azar ni abras puertos del router. Si el firmware ofrece otra forma de depuración, inspecciónala antes de actuar.

Verifica que el estado sea device. Antes de instalar guarda localmente:
- Modelo, Android, usuario activo, versión y firma del launcher instalado.
- Componente HOME que resuelve el sistema y launchers disponibles.
- APK anterior de Glass Home, si existe, y registro de cambios/restauración.

Los respaldos del script contienen el HOME, usuario y APK anterior; no son copia completa de los datos de la TV. Si HOME ya era Glass Home, no lo describas como respaldo del inicio de fábrica. Guarda respaldos, direcciones y claves fuera del proyecto público.

4. VERIFICAR E INSTALAR

Compara el SHA-256 del APK con su fila en SHA256SUMS de la misma Release. En Windows:
  Get-FileHash .\downloads\GlassHome-1.6.0.apk -Algorithm SHA256
  Get-Content .\downloads\SHA256SUMS

No instales si no coincide. El ZIP de la versión incluye el APK y los scripts. Para actualización, compara versión y firma; no intentes bajar de una versión superior a la 1.6.0. Conserva datos y configuración.

En Windows, desde el proyecto:
  .\scripts\install.ps1 -Device IP:PUERTO_DE_CONEXION -Adb RUTA_A_ADB

El instalador también verifica el SHA-256 antes de conectar/instalar, registra el usuario Android activo y guarda respaldo. Sin -SetHome solo abre el launcher; no cambia DNS ni deshabilita paquetes. Si se omite -Device pregunta por la dirección. Lee y comprueba cada resultado.

Si PowerShell bloquea el script, no desactives la política global: utiliza comandos manuales después de respaldar:
  adb -s IP:PUERTO install -r GlassHome-1.6.0.apk
  adb -s IP:PUERTO shell pm grant --user USUARIO_ACTIVO local.glasshome android.permission.READ_TV_LISTINGS
  adb -s IP:PUERTO shell am start -n local.glasshome/.MainActivity

Sustituye USUARIO_ACTIVO por el valor comprobado con am get-current-user. Si un permiso no está disponible, explica la limitación y sigue con las funciones independientes. No declares éxito cuando un comando falla.

La firma de la distribución permite actualizar sus versiones anteriores. Los APK de CI o compilaciones propias pueden usar otra firma: no desinstales automáticamente para eludir un conflicto. Las claves privadas no están en el repositorio. Si solicito una variante, explica la firma y los límites de futuras actualizaciones antes de instalarla, conserva el APK anterior y utiliza el código y wrapper existentes.

5. COMPROBAR CONTENIDO Y CONTROL

Pídeme probar con el control físico:
- Dock en orden, flechas, OK, Atrás, cuadrícula, Ajustes y HOME.
- Volver desde una app sin reconstrucciones visibles, sin saltos del foco y con imágenes completas.
- Desde Netflix en el dock, arriba lleva a la recomendación; desde las demás apps, a Continuar viendo. Abajo abre la cuadrícula.
- En la recomendación, OK abre detalles, derecha lleva a continuaciones y arriba a categorías. Desde una continuación, arriba va a categorías y abajo al dock.
- En categorías, izquierda/derecha elige, OK aplica y arriba va al fútbol. En fútbol, Hoy/Mañana y los detalles disponibles; derecha desde Mañana lleva al botón de Equipos y después a Wi-Fi. En Equipos, OK abre el selector; arriba/abajo y OK recorren y marcan opciones. El buscador acepta nombres y países sin distinguir tildes.
- Voz, volumen, Bluetooth, botones de acceso directo y reproducción real en mis servicios. Abrir una app no prueba que reproduzca.

Continuar viendo:
Pídeme reproducir un título, dejarlo a medias y volver a Home. Verifica tarjeta, app correcta y reanudación, incluyendo elección de perfil cuando aparezca. Lee solo Android TV Watch Next compartido, no bases privadas. Conserva la restricción del enlace a la app que lo publicó. Un permiso concedido no inventa tarjetas. TEVEO, Xuper y otros servicios solo aparecen si publican registros compatibles. NO modifiques, descompiles, parches ni sustituyas sus APK; no prometas recuperar su historial.

Recomendaciones:
Son editoriales del catálogo público de Apple: películas de España y series de EE. UU.; no personalizadas y no reciben mi historial. Caché de catálogo de 24 horas, reintentos de 15 minutos; solo durante el uso. Comprueba detalles y enlace a la fuente. Un título puede requerir compra/alquiler y no estar en mis suscripciones. No inventes carteles o disponibilidad.

Fútbol:
Usa Fixtur.es iCalendar, no extracción de FotMob. De uno a cinco favoritos elegidos en la TV; los cinco originales se conservan al actualizar desde versiones sin selector. Horas de la zona de la TV, caché de seis horas y reintentos de 15 minutos. No incluye marcadores en directo. Comprueba Hoy/Mañana, estado vacío y partidos reales cuando haya. No inventes encuentros para llenar el panel. Los datos guardados pueden mostrarse mientras se actualizan. Comprueba guardar y volver a abrir el selector, cancelar sin cambiar preferencias y Originales antes de guardar. Si ya había favoritos personalizados, no los restablezcas sin pedírmelo. Solo se consultan los calendarios elegidos; la búsqueda del catálogo es local. Equipos sin escudo incluido usan iniciales neutrales y no el escudo de otro club.

6. HOME, REINICIO Y RESTAURACIÓN

Después de las pruebas y según mi elección, selecciona el launcher en Ajustes si el firmware lo permite. Como alternativa:
  adb -s IP:PUERTO shell cmd package set-home-activity --user USUARIO_ACTIVO local.glasshome/.MainActivity
  adb -s IP:PUERTO shell input keyevent KEYCODE_HOME

El script admite -SetHome como opción explícita. Verifica físicamente HOME; que Android acepte el comando no garantiza que el fabricante lo respete.

Si no funciona, inspecciona ese firmware. No deshabilites paquetes con listas genéricas ni borres el launcher de fábrica. En algunos Xiaomi un asistente puede ser propietario de la Wi-Fi guardada: no lo deshabilites ni hagas olvidar la red por rutina. Cualquier intervención adicional necesita explicar el componente, dependencias, restauración y mi autorización específica.

Reinicia una vez y pídeme comprobar HOME, red, control y reproducción. Si hace falta reconectar, solicita la dirección actual. Si falla algo, restaura el APK anterior con firma compatible cuando Android permita esa versión; no uses downgrade forzado ni borrado de datos. La vuelta al inicio de fábrica es independiente de desinstalar Glass Home.

Para restaurar HOME, usa Ajustes o el componente exacto guardado. En Windows:
  .\scripts\restore-home.ps1 -Device IP:PUERTO -Adb RUTA_A_ADB -BackupFile .\backups\CARPETA\home.json
El script valida el usuario del respaldo frente al activo. Si el componente guardado ya era Glass Home, elige el inicio de fábrica desde Ajustes.

7. CIERRE Y ENTREGA

Cuando terminemos las pruebas, guíame para revocar autorizaciones, apagar Wireless/USB/Network/ADB debugging donde existan y reiniciar. Glass Home no requiere depuración para funcionar. Distingue conexión rechazada de timeout; probar un puerto no demuestra que todos estén cerrados.

Entrega versión instalada, pruebas realizadas, limitaciones y pendientes, ubicación privada de respaldos y pasos de restauración. No declares reanudación, rendimiento o pruebas físicas satisfactorias sin evidencia y mi confirmación cuando corresponda.

No publiques mis IP, redes, códigos, contraseñas, cuentas, historiales, respaldos ni claves. Si quiero compartir capturas con datos personales, solicita autorización específica indicando qué datos y dónde se publicarán.
```
