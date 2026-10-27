# Compilar y personalizar

## Herramientas

- JDK 17.
- Android SDK Platform 35 y Build Tools compatibles con Android Gradle Plugin 8.9.2.
- Gradle 8.11.1 mediante el wrapper incluido.

Abre esta carpeta como proyecto en Android Studio y deja que detecte el SDK. En terminal puedes configurar `ANDROID_HOME`, o crear un `local.properties` privado con `sdk.dir=RUTA_DEL_SDK`. No subas ese archivo.

```sh
bash ./gradlew assembleDebug lintDebug --no-daemon
```

En Windows utiliza `gradlew.bat`. El APK de depuración queda en `app/build/outputs/apk/debug/app-debug.apk`. Android genera una clave de depuración local; esta no coincide necesariamente con la distribución oficial ni con la de otro desarrollador.

## Comprobaciones de calendarios

Desde la raíz, crea una carpeta temporal y ejecuta:

```sh
mkdir -p build/fixture-checks
javac -encoding UTF-8 -d build/fixture-checks app/src/main/java/local/glasshome/FootballFixtures.java checks/FootballFixturesCheck.java
java -cp build/fixture-checks local.glasshome.FootballFixturesCheck
```

Hay 14 comprobaciones sin red. Cubren medianoche UTC frente al día local, zonas IANA, duplicados entre favoritos, cancelaciones, valores inválidos, líneas plegadas y respuestas HTML. Las otras 10 comprobaciones opcionales necesitan cinco archivos de calendario descargados por el evaluador; no se publican historiales o cachés de ningún dispositivo.

## Personalización

- `app/src/main/res/values/config.xml`: número de slots del dock (4 a 7) y paquetes de apps en orden.
- `MainActivity.java`: cabecera, fondo, dock y cuadrícula. Fecha centrada arriba y fútbol de 224 × 92 dp debajo; recomendación a la izquierda (54 %) y continuaciones a la derecha (46 %). Wi-Fi y Ajustes quedan a la derecha.
- `LiquidSurface.java`: superficies de cristal mediante degradados y bordes, sin blur continuo.
- `FootballFixtures.java`: lector iCalendar y los cinco favoritos predeterminados, conservados para migración. No representa todos los equipos disponibles.
- `FootballTeams.java` y `app/src/main/assets/football-teams.tsv`: catálogo local, búsqueda, validación y preferencias. Identificadores únicos y seguros; de uno a cinco favoritos. El widget usa una instantánea de la selección para cada carga y rechaza publicaciones de generaciones anteriores.
- `FootballRow.java`: presentación y caché de calendario. Respeta el TTL de la fuente y distingue una descarga fallida de un día sin partidos.
- `RecommendationPanel.java`: feeds públicos de Apple (películas ES, series US), caché de 24 horas, reintentos de 15 minutos, HTTPS con dominios permitidos y detalles con enlace al catálogo. No envía historial ni promete disponibilidad en otros servicios.
- `WatchNextRow.java`: imágenes completas con FIT_CENTER y ancho calculado según su proporción, limitado al espacio disponible; muestreo hasta 960 × 720. Lectura de listados compartidos. No implementa la publicación que corresponde a cada servicio de streaming.

Los escudos incluidos corresponden a los cinco equipos iniciales y se resuelven por identidad, no por posición de selección. Los demás usan iniciales neutrales. Si amplías el catálogo, conserva el formato TSV y verifica los enlaces del proveedor; no asignes un escudo ajeno.

## Firmar una distribución

Las claves privadas **no están en el repositorio**. Para producir tu propia distribución configura en tu entorno:

- `GLASSHOME_KEYSTORE`: ruta absoluta a tu keystore.
- `GLASSHOME_STORE_PASSWORD`.
- `GLASSHOME_KEY_ALIAS`.
- `GLASSHOME_KEY_PASSWORD`.

```sh
bash ./gradlew assembleRelease lintRelease --no-daemon
```

Sin esas variables, `assembleRelease` puede generar un APK sin firma que no es instalable directamente. Mantén la misma clave para futuras actualizaciones de tu distribución. Los builds de CI son de depuración y sirven para validar código; no sustituyen al APK oficial para actualizar una instalación existente.

## Alcance

Este repositorio contiene el launcher. TEVEO y los otros reproductores evolucionan por separado. Para integrar una app con Continue Watching se necesita que ella publique Android TV Watch Next, sus enlaces de reanudación y el progreso del perfil correcto.

## Regresiones de rendimiento 1.5.0

MainActivity conserva el catálogo mientras vive la actividad y lo invalida por broadcasts de paquetes. Mantiene un adapter de cuadrícula con tipo de vista compartido, fuentes en caché y cargas secundarias diferidas. WatchNextRow reutiliza tarjetas por clave de publicación, actualiza progreso/título y deduplica descargas. RecommendationPanel conserva la recomendación en memoria y muestra disco antes de red. FootballRow evita reconstruir un estado igual y reutiliza los datos en regresos rápidos.

Las comprobaciones Android están en app/src/androidTest/java/local/glasshome/PerformanceChecks.java. Compila con `./gradlew assembleDebug assembleDebugAndroidTest`; instala el APK objetivo y el APK de test firmados con la misma clave, usando `adb -s IP:PUERTO install -t` para el de test. Ejecuta:

```sh
adb -s IP:PUERTO shell am instrument -w local.glasshome.test/local.glasshome.PerformanceChecks
```

Comprueban vistas reales: cinco ciclos de pausa/regreso sin recrear dock/adapter ni volver a escanear apps, reciclaje compartido, cambios de progreso/título, foco al reordenar, eliminación y estado vacío. No escriben en el proveedor Watch Next. Las tarjetas sintéticas solo viven durante la prueba; al terminar se recargan los registros compartidos. El APK de test se retira después. CI compila las pruebas; su ejecución física se documenta por separado en [Rendimiento 1.5.0](RENDIMIENTO-1.5.0.md).

## Selector de equipos 1.6.0

El catálogo se lee localmente y precalcula las claves de búsqueda normalizadas. La selección se guarda en preferencias privadas como `football-teams-v1`; si no hay una selección válida se conservan los cinco originales. Cancelar no escribe, Originales prepara un borrador y Guardar valida uno a cinco identificadores. Cachés y reintentos se identifican por slug; un resultado de una selección anterior no sustituye la actual.

Pruebas de modelo sin red:

```sh
javac -encoding UTF-8 -d build/team-checks app/src/main/java/local/glasshome/FootballFixtures.java app/src/main/java/local/glasshome/FootballTeams.java checks/FootballTeamsCheck.java
java -cp build/team-checks local.glasshome.FootballTeamsCheck
```

Pruebas del diálogo real, después de instalar los APK objetivo y de test con la misma firma:

```sh
adb -s IP:PUERTO shell am instrument -w -e suite teams local.glasshome.test/local.glasshome.PerformanceChecks
```

Comprueban búsqueda, cancelación, guardado desde un filtro, selección recuperada, límite de cinco, borrador Originales, rechazo del vacío y persistencia al recrear la actividad. Restauran las preferencias al terminar. Más detalles en [Equipos](EQUIPOS.md).
