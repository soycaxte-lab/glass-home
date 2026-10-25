# Glass Home 1.5.0-fast: cambios, pruebas y mediciones

La versión 1.5.0 (versionCode 12) conserva el diseño de cristal de la 1.4.1 y optimiza el regreso a Home. No se afirma que sea el launcher más rápido de todos ni que acelere la reproducción de servicios externos.

## Cambios

- Catálogo de aplicaciones en memoria durante la vida de la actividad. Los broadcasts de instalación, actualización, eliminación y cambios de paquetes lo invalidan. Regresar desde una app no vuelve a buscar iconos ni reconstruye el dock.
- Un adapter de cuadrícula persistente con tipo de vista compartido y actualización de contenido al reutilizarla.
- Fuentes por peso en caché, menos fondos duplicados y sin reiniciar la animación de cristal cuando su destino es el mismo.
- Continuar viendo reutiliza tarjetas por clave, mantiene el foco, actualiza título/progreso y reordena o elimina lo necesario. Las consultas concurrentes se agrupan y las imágenes en curso no se descargan varias veces.
- Recomendación conservada en memoria; el catálogo guardado se muestra antes de esperar a la red. Se conservan 24 horas de caché y 15 minutos entre reintentos; la memoria no renueva artificialmente un catálogo de disco vencido.
- El fútbol conserva los datos en regresos rápidos y no reconstruye una presentación idéntica. Mantiene seis horas de caché de calendarios y los reintentos de 15 minutos.
- Las cargas secundarias se programan para un siguiente cuadro. No se añade un servicio permanente ni se necesita ADB para usar el launcher.
- Instalador con SHA-256 obligatorio, dirección interactiva si falta, respaldo del usuario Android activo y restauración que rechaza un respaldo de otro usuario.
- Prompt en español con entrevista, preparación, descarga, respaldo, instalación, pruebas, HOME, restauración y cierre de depuración. Explica las personalizaciones que requieren compilar una variante.

## Comparación en el Xiaomi de referencia

Xiaomi MiTV-AFMU0, Android 14. Compilaciones de distribución sin depuración y con la misma firma, en el mismo dispositivo y conservando sus datos. Ocho muestras por caso, con reposo breve entre aperturas. Se midió `WaitTime` de `am start -W`: incluye el tiempo que Android espera para abrir o traer al frente la actividad; no mide cuándo terminan todas las imágenes o solicitudes externas.

| Mediana | Código 1.4.1 | 1.5.0 final | Diferencia observada |
|---|---:|---:|---:|
| Inicio en frío | 904 ms | 829,5 ms | 8,2 % menos |
| Regreso desde Ajustes | 46 ms | 33,5 ms | 27,2 % menos |
| Tiempo de cuadro p50, mediana de tres recorridos | 27 ms | 24 ms | 11,1 % menos |

Para la navegación se fijó el foco inicial en Netflix, se esperaron dos segundos, se reinició gfxinfo y se repitió derecha/derecha/izquierda/izquierda diez veces por muestra, tres muestras por versión. El p95 fue 32/44/44 ms en la base y 26/42/42 ms en la nueva. Los cuadros con retraso fueron 41/1885 en la base y 37/1922 en la nueva; la diferencia es pequeña y no demuestra ausencia de tirones.

La instantánea PSS posterior fue 156833 KiB frente a 137786 KiB (aproximadamente 153,2 frente a 134,6 MiB). Es una lectura por versión y depende de cachés y del estado del sistema; no garantiza un consumo fijo.

Para volver a ejecutar el código anterior sin borrar datos ni forzar un downgrade, se recompiló su fuente de la revisión 7f6e67c con contador 11 y etiqueta de benchmark. La distribución final usa contador 12; los contadores intermedios no se publican como versiones para usuarios.

Las muestras son consecutivas en un solo equipo, sin aleatorización ni control térmico de laboratorio. La red, el firmware, las apps y el contenido pueden cambiar los resultados. [Muestras numéricas y metodología de versiones](performance-1.5.0.json). No contienen direcciones, cuentas ni historial.

## Pruebas

- `assembleRelease lintRelease`: 0 errores y 26 advertencias.
- 14 comprobaciones de iCalendar sin red.
- 12 comprobaciones de dispositivo sobre el APK final: carga de apps, cinco ciclos de pausa/regreso conservando dock/adapter/sin rescan, tipo reciclable compartido, actualización de progreso y título, foco estable, reordenación, eliminación y estado vacío.
- 10 comprobaciones del instalador con ADB simulado: hash incorrecto, ausencia de conexión antes de verificar, respaldo, usuario correcto y rutas de error. No usan red ni una TV.
- El propietario confirmó funcionamiento con el control, regreso con HOME y una tarjeta de Continuar viendo tras instalar la versión final.

CI compila el launcher y las comprobaciones Android, ejecuta iCalendar y el instalador simulado. Las pruebas Android físicas se ejecutaron por separado; compilar su APK en CI no equivale a ejecutarlas allí.

## Repetir las mediciones

Conecta ADB a tu propio equipo, mantén el control inactivo y ejecuta desde el proyecto:

```sh
python checks/measure_home.py --device IP:PUERTO --label mi-medicion
```

Usa `--adb RUTA_A_ADB` si no está en PATH. Necesitas Python 3 y Platform Tools. El script mide la versión que ya está instalada: no instala APK, no cambia HOME ni borra datos, pero cierra y abre temporalmente el launcher, abre Ajustes y mueve el foco. Guarda resultados privados en `backups/performance/`, ignorado por Git. No publiques registros completos sin revisarlos.

[Descargar Release](https://github.com/soycaxte-lab/glass-home/releases/tag/v1.5.0) · [Prompt completo](PROMPT-INSTALACION-ES.md) · [Tutorial](TUTORIAL-ES.md)
