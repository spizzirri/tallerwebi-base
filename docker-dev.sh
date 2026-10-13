#!/bin/sh
# Dev entrypoint: runs mvn jetty:run and watches for .java changes to recompile
# and restart the container. Template hot-reload works via Thymeleaf cache=off.
set -e

# Start jetty in background. seed.demoPlans: [PPV] feed con usuarios/planes fantasma de demo.
mvn jetty:run -DskipTests -Pdev -Dseed.demoPlans=true &
JETTY_PID=$!

# Wait for target/classes to exist (first compilation)
while [ ! -d target/classes ]; do sleep 1; done

# Capture initial timestamps of all .java files
INITIAL_STAMPS=$(find src/main -name '*.java' -printf '%T@\n' 2>/dev/null | sort -rn | head -1)
LAST_STAMPS="$INITIAL_STAMPS"

echo "[dev-watch] Watching src/**/*.java for changes..."

while kill -0 "$JETTY_PID" 2>/dev/null; do
    sleep 2

    CURRENT_STAMPS=$(find src/main -name '*.java' -printf '%T@\n' 2>/dev/null | sort -rn | head -1)

    if [ "$CURRENT_STAMPS" != "$LAST_STAMPS" ]; then
        echo "[dev-watch] Java change detected, recompiling..."
        mvn compile -q -Pdev -DskipTests 2>/dev/null || true
        LAST_STAMPS="$CURRENT_STAMPS"
        echo "[dev-watch] Recompiled. Restarting container..."
        kill "$JETTY_PID" 2>/dev/null
        wait "$JETTY_PID" 2>/dev/null
        exit 1  # Non-zero exit → Docker restarts container
    fi
done

wait "$JETTY_PID"