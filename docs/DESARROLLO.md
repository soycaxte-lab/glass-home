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
- `MainActivity.java`: cabecera, fondo, dock y cuadrícula. El fútbol usa una tarjeta centrada de 216 × 104 dp. Wi-Fi y Ajustes quedan a la derecha.
- `LiquidSurface.java`: superficies de cristal mediante degradados y bordes, sin blur continuo.
- `FootballFixtures.java`: los cinco identificadores de calendarios y sus etiquetas. Mantén la misma cantidad y el orden entre `SLUGS` y `NAMES` si los cambias; la interfaz actual está configurada para cinco calendarios.
- `FootballRow.java`: presentación y caché de calendario. Respeta el TTL de la fuente y distingue una descarga fallida de un día sin partidos.
- `WatchNextRow.java`: lectura de listados compartidos. No implementa la publicación que corresponde a cada servicio de streaming.

Los escudos actuales corresponden a los cinco equipos iniciales. Si sustituyes calendarios, actualiza también los identificadores gráficos y sus avisos; no dejes el escudo de otro equipo.

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
