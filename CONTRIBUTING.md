# Contribuir

Abre un issue o pull request con el problema, el resultado esperado y cómo lo comprobaste. Para cambios de interfaz adjunta una captura sin datos personales y prueba el control remoto.

Ejecuta `assembleDebug lintDebug` y las comprobaciones de calendarios descritas en `docs/DESARROLLO.md`. No incluyas claves de firma, `local.properties`, cachés, respaldos, historiales ni datos de una TV real.

Los cambios de HOME deben ser reversibles y específicos del equipo. No añadas desinstalaciones, borrado de datos o deshabilitación automática de componentes del sistema a la instalación normal.

Las integraciones de streaming leen únicamente los listados compartidos de Android TV. No extraen bases privadas ni credenciales de reproductores.
