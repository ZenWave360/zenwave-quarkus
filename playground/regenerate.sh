#!/usr/bin/env bash
# Reinstall this repo's templates and regenerate a playground app.
#   playground/regenerate.sh [app] [--force|--clean]
#   (default)  write only missing files; hand-edited files are kept
#   --force    overwrite every generated file in src/
#   --clean    delete src/main/java, src/test/java, test data, then generate (fresh template output)
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
APP_NAME="order-fulfillment-quarkus"; MODE=""
for a in "$@"; do case "$a" in --force|--clean) MODE="$a";; *) APP_NAME="$a";; esac; done
APP="$ROOT/playground/$APP_NAME"
mvn -B -q -f "$ROOT/pom.xml" install -DskipTests
FORCE=false
if [ "$MODE" = "--clean" ]; then rm -rf "$APP/src/main/java" "$APP/src/test/java" "$APP/src/test/resources/data" "$APP/src/test/resources/data.sql"; fi
if [ "$MODE" = "--force" ]; then FORCE=true; fi
mvn -B -q -f "$APP/pom.xml" -Pzenwave initialize -Dzenwave.forceOverwrite=$FORCE
