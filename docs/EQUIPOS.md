# Cambiar equipos en Glass Home 1.6.0

La versión **1.6.0-teams** (versionCode 13) añade selección desde la propia TV. No necesitas editar código, recompilar ni usar ADB para cambiar tus favoritos.

## Con el control remoto

1. Sube al widget **Fútbol**. Desde las categorías del inicio, pulsa ↑; también puedes usar el balón de la cabecera.
2. Recorre **Hoy → Mañana → ⚙** con →. Pulsa OK en el engranaje para abrir **Mis equipos de fútbol**.
3. Busca por nombre, selección o país. La búsqueda no distingue tildes. Los nombres iguales se diferencian por su país, por ejemplo Liverpool de Inglaterra y Liverpool Montevideo de Uruguay.
4. Marca de uno a cinco favoritos. Si ya tienes cinco, desmarca alguno antes de añadir otro.
5. Pulsa **Guardar**. El widget elimina los datos de la selección anterior y carga los calendarios de la nueva; mientras descarga puede indicar que hay calendarios pendientes.

**Cancelar** y Atrás cierran sin guardar. **Originales** prepara Real Madrid, Bayern Múnich, Barcelona, Atlético Nacional y selección de Colombia; pulsa Guardar para aplicar esa restauración. No se permite guardar una selección vacía.

![Selector real de equipos en un emulador Android TV](screenshots/selector-equipos.png)

La imagen procede de la aplicación ejecutándose en un emulador de Android TV. No es una captura del Xiaomi ni un mockup generado.

## Guardado y actualización

Los favoritos se guardan localmente y se conservan al cerrar, recrear la actividad y actualizar el APK con firma compatible. La primera actualización desde una versión sin selector mantiene los cinco favoritos originales. No desinstales ni borres datos para actualizar si quieres conservar la configuración.

La búsqueda del catálogo funciona sin conexión. Solo se descargan los calendarios elegidos, con los mismos límites de seis horas de caché y al menos 15 minutos entre reintentos. Las cachés se identifican por equipo, no por el puesto en la lista. Un resultado tardío de los equipos anteriores no sustituye los nuevos. No se añaden servicios permanentes.

Se mantienen los horarios locales, Hoy/Mañana y la deduplicación de encuentros entre favoritos. El widget no incluye transmisiones ni marcadores en directo. La fuente puede no tener encuentros actuales o futuros para un equipo; no se inventan partidos para llenar el panel.

Los escudos incluidos corresponden a los cinco equipos originales y siguen a su identidad aunque cambies el orden. Otros equipos usan iniciales neutrales; no se les asigna el escudo de otro club.

## Catálogo incluido

Incluye **296 clubes y selecciones**, a partir de enlaces públicos de Fixtur.es. Es una instantánea del catálogo disponible al preparar esta versión; no representa todos los equipos del mundo y no se actualiza continuamente durante el uso. Si no aparece un equipo, su incorporación requiere ampliar el catálogo en una actualización. No se necesita una cuenta ni una clave API.

Fuentes del catálogo:

- [España](https://fixtur.es/es/primera-division), [Alemania](https://fixtur.es/es/bundesliga), [Inglaterra](https://fixtur.es/es/premier-league), [Italia](https://fixtur.es/es/serie-a), [Francia](https://fixtur.es/es/ligue-1), [Portugal](https://fixtur.es/es/superliga), [Países Bajos](https://fixtur.es/es/eredivisie) y [Escocia](https://fixtur.es/es/scottish-premier-league).
- [Colombia](https://fixtur.es/es/primera-a), [Argentina](https://fixtur.es/es/primera-division-argentina), [Brasil](https://fixtur.es/es/serie-a-brazil), [Chile](https://fixtur.es/es/primera-division-de-chile) y [Uruguay](https://fixtur.es/es/uruguay-primera-division).
- [México](https://fixtur.es/es/liga-mx) y [MLS](https://fixtur.es/es/mls-major-league-soccer).

La presencia en una página de origen no garantiza que el proveedor mantenga un calendario activo o completo. Nombres, escudos, marcas y metadatos conservan los derechos de sus titulares; no se convierten en código MIT del proyecto. Consulta [los avisos de terceros](../THIRD_PARTY_NOTICES.md).

## Comprobaciones

La validación de modelo incluye 20 comprobaciones sin red: migración de favoritos, orden, duplicados, entradas inválidas, búsqueda por tildes/país, equipos con nombre parecido, límite de cinco y asociación correcta de escudos. Las pruebas de diálogo Android verifican buscar, guardar, cancelar, reabrir, Originales, rechazar vacío y recrear la actividad; instrucciones en [Desarrollo](DESARROLLO.md).

Se ejecutaron 11 comprobaciones del diálogo y 12 regresiones de navegación en un emulador Android TV (API 36), y se revisó la captura real. Además, pasaron las 14 comprobaciones del lector iCalendar y las 10 del instalador. Compilación y lint: 0 errores, 34 advertencias. La prueba física de esta versión en el Xiaomi sigue pendiente de conexión.

El APK conserva la firma de las distribuciones anteriores. Las mediciones de rapidez publicadas para la 1.5.0 son históricas; no se han repetido como benchmark de la 1.6.0.

[Descargar Release](https://github.com/soycaxte-lab/glass-home/releases/tag/v1.6.0) · [Tutorial](TUTORIAL-ES.md) · [Prompt guiado](PROMPT-INSTALACION-ES.md)
