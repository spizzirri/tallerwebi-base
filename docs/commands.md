# Commands Reference

## Maven

To run Maven commands, either in the IDE's integrated terminal or in another terminal, use the main `mvn` command followed by the command or lifecycle phase to execute.

> Maven runs all the phases prior to the lifecycle phase you specify.

### Lifecycle phases

```shell
# Cleans the target directory from the previous build
mvn clean

# Validates that the project is correct
mvn validate

# Compiles the project source code
mvn compile

# Runs the Java test suites (unit + MockMvc integration)
mvn test

# Packages the compiled code into a JAR or WAR file
mvn package

# Verifies that the package is valid
mvn verify

# Installs the package into the local Maven repository
mvn install
```

### Common combinations

```shell
# Most common , downloads dependencies, compiles, and runs tests
mvn clean install

# Clean build with tests
mvn clean package

# Run tests only
mvn test
```

### Development server

```shell
# Start Jetty with hot-reload (template + JS changes auto-refresh)
mvn jetty:run

# Full rebuild + start
mvn clean jetty:run
```

Jetty runs at [http://localhost:8080](http://localhost:8080). Java changes require a restart; template and vendored JS changes reload live.

## Docker

### Start / stop

```shell
# Start PostgreSQL + app with hot-reload
docker compose up

# Stop and remove containers + volumes
docker compose down --rmi local

# Throw the local database away and rebuild it -- DESTROYS ALL LOCAL DATA
.\scripts\gate.ps1 reset-db
```

`reset-db` is the local remedy for a schema that no longer matches the entities, because
`hibernate.hbm2ddl.auto=update` never drops a column. See
[setup.md](setup.md#database-schema).

### Common commands

```shell
# Show running containers
docker ps

# Show all containers
docker ps -a

# Show all images
docker images

# Show container logs
docker logs <containerId>

# Remove a container
docker rm <containerId>

# Remove an image
docker rmi <imageId>

# Run a container with bash
docker run -it --entrypoint /bin/bash valhalla
```

## Testing

```shell
# Run all Java tests (uses in-memory HSQLDB, no PostgreSQL needed)
mvn test

# Same, skipping Checkstyle/PMD/CPD/Prettier/JaCoCo -- fastest loop
mvn test -Pdev

# One entry point for every gate: .\scripts\gate.ps1 <command>[=<target>] [options]
.\scripts\gate.ps1 list                     # every class per layer, with test counts
.\scripts\gate.ps1 list UserServiceTest     # the methods in one class, fully qualified
.\scripts\gate.ps1 unit                     # unit tests only
.\scripts\gate.ps1 integration              # MockMvc integration tests only
.\scripts\gate.ps1 e2e                      # E2E only, brings up PostgreSQL + Chromium
.\scripts\gate.ps1 e2e -Headed -SlowMo 300   # watch the browser
.\scripts\gate.ps1 e2e=LoginViewE2E         # run only what you name
.\scripts\gate.ps1 all                      # unit + integration + E2E in one `mvn verify`
.\scripts\gate.ps1 check                    # Checkstyle, PMD, CPD, Prettier; changes no files
.\scripts\gate.ps1 js                       # the frontend JS contract (docs/guide-js.md)
.\scripts\gate.ps1 js -Strict               # same, but fail on a violation instead of reporting
.\scripts\gate.ps1 coverage                 # the whole suite, then the line coverage table
.\scripts\gate.ps1 unit -Fast               # skip the static-analysis gates
```

Bare `.\scripts\gate.ps1` prints help. Nothing runs unless you name a layer.

Git Bash runs the same thing: `pwsh -c '.\scripts\gate.ps1 e2e -Headed -SlowMo 300'`.

For the full E2E setup — the dedicated database, `DB_NAME`, headed mode and reading failure
artifacts — see [testing.md](testing.md#e2e-tests). That section is canonical; this file only
lists the one-line commands.

## CI/CD (GitHub Actions)

The pipeline runs on every push and PR to `main`. It has two jobs:

### `backend` , build + test + quality gates

Runs `mvn clean verify --fail-at-end -DskipITs` which triggers:
1. Prettier formatting (check only; fix with `gate.ps1 check -Fix`)
2. Checkstyle (naming, Javadoc, imports)
3. PMD + CPD (logic issues, duplication)
4. Unit + integration tests (HSQLDB)
5. JaCoCo coverage check (80% floor, 100% for domain/presentation)

If any gate fails, the build fails.

### `e2e` , Playwright against a real stack

1. Spins up a PostgreSQL service container holding a dedicated `valhalla_e2e` database
2. Installs Playwright's Chromium, pinned to the version in `pom.xml`
3. Packages the app (`mvn package -DskipTests` with the static-analysis gates skipped, since
   the `backend` job already enforced them and this job `needs: backend`)
4. Starts Jetty against the local Postgres and waits up to 120s for the app root to answer
5. Runs E2E tests (`LoginViewE2E`, `UserViewABME2E`) via
   `mvn failsafe:integration-test failsafe:verify`
6. Uploads `target/failsafe-reports/` and `target/e2e-artifacts/` (per-test screenshot and
   Playwright trace) as an artifact, even when the run fails

**To run the full pipeline locally before pushing:**

```shell
mvn clean verify
```
