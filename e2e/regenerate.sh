#!/usr/bin/env bash
# Reinstall this repo's templates and regenerate an e2e project in place (src/test/resources/projects/<name>).
#   e2e/regenerate.sh [name] [--force|--clean]
#   (default)  write only missing files; hand-edited files are kept
#   --force    overwrite every generated file in src/
#   --clean    delete src/main/java, src/test/java and test data, then generate (fresh template output)
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
NAME="order-fulfillment-quarkus"; MODE=""
for a in "$@"; do case "$a" in --force|--clean) MODE="$a";; *) NAME="$a";; esac; done
APP="$ROOT/e2e/src/test/resources/projects/$NAME"
[ -f "$APP/pom.xml" ] || { echo "no project: $APP" >&2; exit 1; }
mvn -B -q -f "$ROOT/pom.xml" install -DskipTests
FORCE=false
if [ "$MODE" = "--clean" ]; then
  rm -rf -- "$APP/src/main/java" "$APP/src/test/java" "$APP/src/test/resources/data" "$APP/src/test/resources/data.sql"
fi
if [ "$MODE" = "--force" ]; then FORCE=true; fi
mvn -B -q -f "$APP/pom.xml" -Pzenwave initialize -Dzenwave.forceOverwrite=$FORCE
