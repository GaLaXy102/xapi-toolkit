# xapi_toolkit

Datengenerierungs- und auswertungsanwendung für xAPI-Kommunikation

## Konfiguration

Die BasicAuth-Zugangsdaten müssen mit den Umgebungsvariablen `DATATOOLS_SEC_USERNAME` und `DATATOOLS_SEC_PASSWORD` gesetzt werden.

## CI/CD

Docker-Images können mit dem Target `spring-boot:build-image` gebaut und hochgeladen werden.
Dafür ist Zugriff auf den Docker-Daemon nötig, sowie folgende Umgebungsvariablen:

|          Variable          | Bedeutung                                            |
|:--------------------------:|:-----------------------------------------------------|
|   `REGISTRY_UPLOAD_HOST`   | Registry für Upload des Docker-Images, ggf. mit Port |
|   `REGISTRY_UPLOAD_USER`   | Nutzername für Push des Docker-Images                |
| `REGISTRY_UPLOAD_PASSWORD` | Passwort für Push des Docker-Images                  |
