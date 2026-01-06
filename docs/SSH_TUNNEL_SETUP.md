## Database Connection (No SSH Tunnel Required)

The application is now configured to connect directly to the PostgreSQL server without using an SSH tunnel.

### Default Connection

- **Database Host**: `196.221.167.63`
- **Database Port**: `5432`
- **Default JDBC URL**: `jdbc:postgresql://196.221.167.63:5432/mydb`

These defaults can be overridden using environment variables:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

### Example: Running Locally

If your database is reachable directly, you can simply run the Spring Boot application:

```bash
mvn spring-boot:run
```

Or override the connection:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://<your-db-host>:5432/mydb \
SPRING_DATASOURCE_USERNAME=<your-user> \
SPRING_DATASOURCE_PASSWORD=<your-password> \
mvn spring-boot:run
```

### Example: Using Docker Compose

The provided `docker-compose.yml` is configured to connect directly to the remote database using environment variables. Adjust the host, database name, username, and password as needed for your environment.

