# Glass Home

Un launcher para **Android TV y Google TV** con una recomendación grande a la izquierda, Continuar viendo a la derecha, iconos inspirados en Apple TV, paneles de cristal y fútbol debajo de la fecha. Versión **1.6.0-teams**.

**[Descargar APK 1.6.0](https://github.com/soycaxte-lab/glass-home/releases/download/v1.6.0/GlassHome-1.6.0.apk)** · [Releases](https://github.com/soycaxte-lab/glass-home/releases/latest) · [Instalación y restauración](docs/INSTALACION.md) · [Compilar y personalizar](docs/DESARROLLO.md)

**[Copiar el prompt completo en español para tu asistente](docs/PROMPT-INSTALACION-ES.md)**. Incluye la instalación de esta interfaz, configuración, pruebas con el control y restauración. Abre el enlace y copia el bloque completo.

**¿Es tu primera instalación? [Sigue el tutorial paso a paso con imágenes](docs/TUTORIAL-ES.md)**: preparación, conexión, instalación, uso del control y solución de problemas.

## Así se ve en la TV

![Glass Home en el Xiaomi: recomendación, Continuar viendo, dock y fútbol de hoy](docs/screenshots/inicio-hoy.png)

Captura real del diseño 1.4.1, conservado en la 1.6.0, que añade el selector de equipos, en el Xiaomi de referencia, sin edición y con Continuar viendo visible, publicada con autorización de su propietario. Las tarjetas, aplicaciones, recomendaciones y partidos dependen de cada dispositivo y de sus proveedores.

**[Ver las capturas de Hoy y Mañana](docs/CAPTURAS.md)** · [Cambios y mediciones de la versión 1.5.0](docs/RENDIMIENTO-1.5.0.md)

## Equipos personalizables en la 1.6.0

Abre **Fútbol → ⚙ → Mis equipos de fútbol**, busca por nombre o país, elige de uno a cinco favoritos y pulsa **Guardar**. Hay 296 clubes y selecciones en el catálogo incluido. Cambiar equipos ya no requiere compilar otra APK. **Originales** prepara los cinco iniciales y **Cancelar** descarta los cambios. [Guía del selector](docs/EQUIPOS.md).

## Rendimiento desde la 1.5.0

Conserva la interfaz y reutiliza el catálogo de aplicaciones, el dock, las tarjetas y las imágenes al volver a Home. Las instalaciones, actualizaciones y eliminaciones de apps invalidan el catálogo. El fútbol y las recomendaciones conservan sus límites de actualización. Consulta las [mediciones y pruebas](docs/RENDIMIENTO-1.5.0.md); no se afirma que sea el launcher más rápido de todos.

## Qué incluye

- Fondo estático azul/violeta con reflejos y paneles translúcidos dibujados con degradados; sin desenfoque continuo ni vídeo de fondo.
- Recomendación editorial de películas o series desde el catálogo público de Apple, con detalles y enlace a su fuente. No es personalizada ni garantiza disponibilidad en tus suscripciones.
- Pestañas Explorar / Películas / Series que también filtran las continuaciones según los datos publicados por cada app.
- Dock inicial: Netflix → TEVEO → Prime Video → YouTube, con los iconos de tus aplicaciones instaladas.
- Navegación por flechas, cuadrícula de apps, reloj y acceso a Wi-Fi y Ajustes.
- Continue Watching mediante **Android TV Watch Next**: títulos, progreso y enlaces de reanudación publicados por las apps compatibles.
- Carteles de Continuar viendo con ancho adaptable a su proporción y la imagen completa; las siguientes tarjetas se recorren con las flechas.
- Fútbol en una tarjeta de 224 × 92 dp centrada debajo de la fecha, con pestañas Hoy/Mañana y horarios según la zona de la TV.
- Selector de hasta cinco favoritos, con búsqueda sin conexión, guardado local y restauración de originales.
- Calendarios iniciales: Real Madrid, Bayern Múnich, Barcelona, Atlético Nacional y selección de Colombia.

## Compatibilidad

Requiere Android TV/Google TV con Android 8.0 o superior. Probado físicamente en un Xiaomi MiTV-AFMU0 con Android 14, a 1920 × 1080. Otros modelos, resoluciones y versiones de firmware necesitan sus propias pruebas. **No es una aplicación para Samsung Tizen ni LG webOS.**

Las apps de streaming se instalan por separado; Glass Home no incluye contenido, suscripciones ni transmisiones. Netflix y Prime Video publicaron tarjetas utilizables en el equipo probado, pero esa disponibilidad puede cambiar entre versiones, cuentas y dispositivos.

**TEVEO debe publicar Watch Next desde su propia app.** Instalar este launcher no añade esa función a TEVEO. Este repositorio no contiene código, APK ni modificaciones de TEVEO. Si una actualización de cualquier app deja de publicar tarjetas, el launcher no puede reconstruir su historial privado.

## Instalación rápida con ADB

1. Descarga el [APK](downloads/GlassHome-1.6.0.apk?raw=true) y [Android Platform Tools](https://developer.android.com/tools/releases/platform-tools).
2. Activa la depuración en la TV y utiliza la dirección **IP:puerto que muestre ahora**. En Android con emparejamiento inalámbrico, empareja primero con el puerto y código de esa pantalla.
3. Instala y abre el launcher:

```sh
adb connect IP:PUERTO
adb -s IP:PUERTO install -r GlassHome-1.6.0.apk
adb -s IP:PUERTO shell pm grant local.glasshome android.permission.READ_TV_LISTINGS
adb -s IP:PUERTO shell am start -n local.glasshome/.MainActivity
```

En Windows también puedes usar [scripts/install.ps1](scripts/install.ps1), que guarda el componente HOME anterior y una copia del APK de Glass Home si ya estaba instalado. **No deshabilita apps del sistema ni cambia DNS.**

Comprueba la navegación antes de seleccionar Glass Home como inicio predeterminado. Consulta [la guía completa](docs/INSTALACION.md) para hacerlo y para volver atrás. Un firmware puede impedir cambiar HOME; no ejecutes listas de deshabilitación tomadas de otro dispositivo.

## Control remoto

| Desde | Acción |
|---|---|
| Dock | ←/→ cambia de app; ↑ desde Netflix va a la recomendación y desde las demás apps a Continuar viendo; ↓ abre la cuadrícula |
| Recomendación | OK abre detalles; → entra en Continuar viendo; ↑ va a las categorías; ↓ vuelve al dock |
| Continuar viendo | ←/→ recorre títulos; ← desde el primero vuelve a la recomendación; ↑ va a categorías; ↓ vuelve al dock |
| Categorías | ←/→ elige y OK aplica; ↑ va a fútbol; ↓ a la recomendación |
| Fútbol | ←/→ cambia Hoy/Mañana; → desde Mañana pasa a Equipos y luego a Wi-Fi |
| Hoy/Mañana | ↓ entra en los partidos; si no hay, vuelve a Continue Watching |
| Partido | OK abre su detalle; ↓ desde el último vuelve a Continue Watching |
| Wi-Fi/Ajustes | ←/→ cambia de control; ← desde Wi-Fi regresa a Fútbol |
| Cualquier sección del launcher | Atrás regresa al dock |

## Datos y privacidad

Continue Watching lee los listados compartidos de Android TV con `READ_TV_LISTINGS`. No entra en bases privadas de otras apps ni solicita sus contraseñas. Los enlaces de reanudación se limitan a la aplicación que publicó cada tarjeta. Las imágenes de esos listados pueden solicitarse a sus proveedores mediante HTTPS.

El panel de fútbol utiliza las [suscripciones iCalendar gratuitas de Fixtur.es](https://fixtur.es/es/team/real-madrid), no extrae datos de FotMob. Mantiene una caché privada, respeta una ventana de seis horas entre actualizaciones de cada calendario y espera al menos 15 minutos después de un fallo. Solo consulta al volver al launcher o al cambiar de día mientras está visible. No incluye marcadores en directo; cobertura y horarios dependen de la fuente.

El descubrimiento consulta feeds públicos de Apple: películas de España y temporadas de series de EE. UU. No envía el historial. Guarda catálogo e imágenes en caché durante 24 horas y espacia los reintentos al menos 15 minutos mientras el launcher está activo. La disponibilidad y el precio dependen del catálogo y la región.

No hay analítica propia, servidor propio ni servicio permanente en segundo plano. La app guarda la última app seleccionada y cachés de calendarios y descubrimiento. Android puede incluir esos datos en sus copias de seguridad. Los proveedores externos reciben las solicitudes de red necesarias para catálogos, imágenes y calendarios.

## Código y validación

Java 17, Android SDK 35, Gradle wrapper y AndroidX RecyclerView. [Cómo compilar](docs/DESARROLLO.md).

La versión 1.5.0 conserva el diseño y añade pruebas de regresión en dispositivo para caché de apps, reciclaje de vistas, progreso y foco. El lector iCalendar incluye 14 comprobaciones reproducibles sin red. La metodología, resultados y límites están en [Rendimiento 1.5.0](docs/RENDIMIENTO-1.5.0.md).

Los builds de GitHub Actions son compilaciones de depuración para comprobar el código; sus firmas pueden variar. Para instalar o actualizar la distribución oficial utiliza el APK de `downloads/` y verifica [SHA256SUMS](downloads/SHA256SUMS).

## Licencias y aportes

Código propio bajo [MIT](LICENSE). La fuente Outfit conserva su [SIL Open Font License](Outfit-OFL.txt). Consulta [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) para dependencias, calendarios y marcas de terceros.

Puedes abrir un issue con tu modelo, versión Android y una descripción del problema. No publiques contraseñas, códigos de emparejamiento, direcciones de proveedores ni capturas con historial personal.
