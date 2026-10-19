# Development Environment Setup

Before working with the project, install and configure these tools.

## Java

Java is the programming language used in this project. The project is configured to run with Java 25 (LTS).

1. Download the JDK for your operating system: [JDK 25 Temurin](https://adoptium.net/temurin/releases/?version=25).
2. Unzip the downloaded file into a folder.
   - Example: `/home/java` (Linux) or `C:\java` (Windows).
3. Set an environment variable named `JAVA_HOME` pointing to that folder.
   - Example: `C:\java\jdk-25` (Windows) or `/home/java/jdk-25` (Linux).
4. Add `JAVA_HOME` to the `PATH` environment variable:
   - Add `%JAVA_HOME%\bin` to the existing list.
5. Run `java -version` and `javac -version` in CMD or Terminal; you should see the installed Java version and the compiler version as output, respectively.

- [Guide to installing Java on Windows](https://www.java.com/es/download/help/windows_manual_download.html)
- [Guide to installing Java on Linux](https://www.java.com/es/download/help/linux_x64_install.html)

## Maven

Maven is a project management tool (mainly for Java projects). It simplifies and standardizes the software build process.

1. Java 25 must be installed and environment variables configured (`JAVA_HOME` and `PATH`).
2. Download Maven from the [official site](https://dlcdn.apache.org/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.zip) and unzip it into a folder.
3. Set an environment variable named `MAVEN_HOME` pointing to that folder.
   - Example: `/home/maven/apache-maven-3.9.16` (Linux) or `C:\maven\apache-maven-3.9.16` (Windows).
4. Add `MAVEN_HOME` to the `PATH` environment variable:
   - Add `%MAVEN_HOME%\bin` to the existing list.
5. Run `mvn -version` in CMD or Terminal; you should see the downloaded Maven version as output.
6. **.m2 folder**: Maven's local repository where it stores artifacts (like JAR files), downloaded as dependencies or generated locally. By default this folder is created in:
   - Linux: `/home/<user>/.m2`
   - Windows: `C:\Users\<user>\.m2`

- [Guide to installing Maven](https://maven.apache.org/install.html)

### IDE configuration

- **IntelliJ**: Maven comes installed and the plugin is available in the panel on the right (shown with the letter **M**). It lets you run commands and manage plugins and dependencies.
- **VS Code** (recommended plugins):
  - **Maven for Java**: the official plugin. From the explorer, a Maven section appears below the project.
  - **XML**: improves autocompletion and syntax validation in XML files. For Maven the `pom.xml` file is crucial.

## Docker

Docker is a container platform that packages applications together with all their dependencies into lightweight, portable containers.

- **Windows**: install `Docker Desktop`. Follow this [guide](https://docs.docker.com/desktop/setup/install/windows-install/).
- **Linux**: install `Docker Engine` by following this [guide](https://docs.docker.com/engine/install/ubuntu/). You can also install `Docker Desktop` (it includes Docker Engine).

## Environment Variables

The app connects to PostgreSQL via environment variables. Copy `.env.example` to `.env` and fill in the values. That file is read by `docker compose` only -- the application does not load it:

| Variable | Read by | Description | Default |
| :--- | :--- | :--- | :--- |
| `APP_DB_HOST` | compose | PostgreSQL host **as seen from the container**, i.e. the `postgres` service name | `postgres` |
| `DB_PORT` | compose | PostgreSQL port | `5432` |
| `DB_NAME` | compose | Database name | `valhalla` |
| `DB_USER` | compose | Database user | `user` |
| `DB_PASSWORD` | compose | Database password | `user` |
| `POSTGRES_DB` | compose | Initial DB created by the Postgres container | `valhalla` |
| `POSTGRES_USER` | compose | Initial superuser created by the Postgres container | `user` |
| `POSTGRES_PASSWORD` | compose | Initial superuser password | `user` |

Compose passes `APP_DB_HOST` to the app as `DB_HOST`. The two names are deliberately different: Compose prefers the shell environment over this file, so a single `DB_HOST` carrying both the container value and the host value would let any host-side `DB_HOST=localhost` silently repoint the container at itself.

When you run the app **outside** Docker (`mvn jetty:run`, `mvn verify` via `scripts/gate.ps1 e2e` or `scripts/gate.ps1 all`), it reads `DB_*` from the process environment instead. Set only what differs from the built-in defaults -- the host needs no `DB_HOST` at all, because `EnvironmentConfig.DEFAULT_DB_HOST` is already `localhost`.

`DB_*` variables are read by the Java app at runtime. `POSTGRES_*` variables are only used by the Postgres Docker container on first boot.

```shell
cp .env.example .env
# Edit .env with your values
```

**Tip:** the defaults work out of the box , you only need to edit `.env` if you want custom credentials.

## Database schema

**The schema does not migrate. It is rebuilt from the entities, once, on a database that has
never seen them before.**

`JpaConfig` sets `hibernate.hbm2ddl.auto=update`, and there is no Flyway and no Liquibase in
`pom.xml`. `update` **adds** missing columns; it never drops and never renames one. That has one
consequence worth stating plainly:

**Renaming or removing a column silently breaks every database that already exists.** The old
column survives, the rows that already had data in it keep it, and they land `NULL` in the new
column — including the primary key, when the id column itself is the one that changed. Those rows
become unreachable through the app: nothing selects them, nothing deletes them, and the orphan
tables and columns accumulate for every such change.

On a database created before ticket `02-PLN`, the stale `plans` columns `id_plan`,
`event_date_created`, `start_time`, `end_time`, `codigo` and `selected_place_id` are still there,
and so is the whole `plan_places` table — **including its `position NOT NULL` column**. A fresh
database has neither. That difference is the time bomb for whoever implements the itinerary under
`04-APL-BE`: the same insert succeeds locally and fails against a database that predates it, with
a constraint violation on a column the code no longer even mentions.

**2026-10: databases created before commit `af0f96f` have `places.description` as an `oid`
large-object column (the old `@Lob` mapping).** The mapping is now `varchar(2048)`, and
`update` never converts the type of an existing column in place. On a stale volume, every read of
a place description — including the ones the demo seeder does at startup — fails with-
`Large Objects may not be used in auto-commit mode`; the `WebAppContext` never deploys and Jetty
answers **HTTP 503 Service Unavailable** for every URI. The remedy is the usual one, run once:

```shell
pwsh -File scripts/gate.ps1 reset-db
```

The local data is reseeded automatically (`docker-dev.sh` seeds with `-Dseed.demoPlans=true`) and
the Maven cache only costs a re-download.

The only remedy for a local database is to throw it away:

```shell
pwsh -File scripts/gate.ps1 reset-db
```

**That destroys all local data, and losing the data is the point**: the volume goes away and
Hibernate builds the schema again from the entities. There is no way to clean this up in place.

Adopting Flyway (or Liquibase) is the durable fix — versioned, reviewable, reversible migrations.
It is a project-wide decision, and this ticket deliberately does not make it.

