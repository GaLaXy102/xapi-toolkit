# xapi_toolkit

Datengenerierungs- und auswertungsanwendung für xAPI-Kommunikation

## Konfiguration

Die BasicAuth-Zugangsdaten müssen mit den Umgebungsvariablen `XAPITOOLS_SEC_USERNAME` und `XAPITOOLS_SEC_PASSWORD` gesetzt werden.

Weiterhin wird ein Zwischenspeicher für die Simulationsergebnisse benötigt. Dieser wird über die Variable `XAPITOOLS_SIM_STORAGE_DIR` gewählt.

Die Verbindungsparameter für DATASIM werden mit den Umgebungsvariablen `XAPITOOLS_SIM_BACKEND_BASE_URL`, `XAPITOOLS_SIM_BACKEND_USERNAME` und `XAPITOOLS_SIM_BACKEND_PASSWORD` festgelegt.

Im produktiven Einsatz müssen die Variablen `XAPITOOLS_DB_CONNECTION_STRING`, `XAPITOOLS_DB_CONNECTION_USER` und `XAPITOOLS_DB_CONNECTION_PASSWORD` mit Zugangsdaten für eine relationale Datenbank wie MariaDB gefüllt werden. Ein Beispiel hierzu findet sich in der [Compose-Datei](docker-compose.yml).

## Hinweise zur Datensicherheit

Die Verbindungsdaten für LRS werden unverschlüsselt gespeichert.

## CI/CD

Docker-Images können mit dem Target `spring-boot:build-image` gebaut und mit der Property `-Ddocker.publish=true` hochgeladen werden.
Dafür ist Zugriff auf den Docker-Daemon nötig, sowie folgende Umgebungsvariablen:

|          Variable          | Bedeutung                                            |
|:--------------------------:|:-----------------------------------------------------|
|   `REGISTRY_UPLOAD_HOST`   | Registry für Upload des Docker-Images, ggf. mit Port |
|   `REGISTRY_UPLOAD_USER`   | Nutzername für Push des Docker-Images                |
| `REGISTRY_UPLOAD_PASSWORD` | Passwort für Push des Docker-Images                  |

## Developers and Administrators

Remember to make `dev` your default application profile while developing. Like that, automatic seeding will happen.

In the Docker Image, the `prod` profile is active by default.

Tests can activate the `test` profile as they wish.

You can build the Javadoc of this project by executing the Maven Goal `javadoc:javadoc`, for example by calling `./mvnw javadoc:javadoc`.

## External Credits

This Application uses third party content:

* [Tin icons created by Smashicons - Flaticon](https://www.flaticon.com/free-icons/tin)