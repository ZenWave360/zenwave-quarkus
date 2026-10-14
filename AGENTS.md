# AGENTS.md

This repository is the base for ZenWave backend customizations. Quarkus is the first target. The pattern, modules, and stack are in `docs/001-target-stack.md`. Target files and template decisions are in `docs/quarkus-customization.md`. Status is `docs/tasks.md`: `[ ]` not started, `[/]` in progress, `[x]` done / ready for review, `[!]` blocked.

Mirror [`../zenwave-sdk/plugins/customizations/kotlin-backend-application`](../zenwave-sdk/plugins/customizations/kotlin-backend-application) ([GitHub](https://github.com/ZenWave360/zenwave-sdk/tree/main/plugins/customizations/kotlin-backend-application)): templates and helpers, selected with the `templates` option on the official plugins.

Before using an `io.zenwave360.sdk` type, method, or model key, read it in `../zenwave-sdk`.

Generated output is Kotlin. Business and application code stays imperative. Templates write sources into an existing project.
