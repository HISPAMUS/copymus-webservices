# CopyMus webservices

## Deployment

```
./mvnw clean package
```

- Create `~/copymus/`, `~/copymus/db/`, and `~/copymus/dist/` folders and copy:
	- `target/copymus-webservices-X.X.X.jar` into `~/copymus/dist/`
	- `src/main/resources/application.properties` into `~/copymus/`
	- `copymus-start.sh` into `~/copymus/`
- Update symbolic link `~/copymus/copymus-latest` to the latest jar
- Edit crontab with `crontab -e`:

```
@reboot cd /home/cperez/copymus && ./copymus-start.sh
```

## Security

All services are secured using an API key. Currently there is only one key registered, it can be changed in `application.properties`:
```
spring.security.user.password=API_KEY
```

## OpenAPI documentation

Swagger UI is available at `/swagger-ui/index.html`. The API key can be specified by pressing the `Authorize` button.

## Database import

For importing Primus database, run `primus.sql` script from the H2 shell:
```
$ java -cp h2*.jar org.h2.tools.Shell
sql> runscript from 'primus.sql';
```
