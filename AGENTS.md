# AGENTS.md

This repository is an example of a ZenWave customization that lives outside the upstream SDK. Quarkus is the first target. The pattern, modules, and stack are in `docs/001-target-stack.md`. Target files and template decisions are in `docs/quarkus-customization.md`. Status is `docs/tasks.md`: `[ ]` not started, `[/]` in progress, `[x]` done / ready for review, `[!]` blocked.

Mirror the project shape of [`../zenwave-sdk/plugins/customizations/kotlin-backend-application`](../zenwave-sdk/plugins/customizations/kotlin-backend-application) ([GitHub](https://github.com/ZenWave360/zenwave-sdk/tree/main/plugins/customizations/kotlin-backend-application)): a Maven jar of Handlebars templates and helpers, with no plugin of its own, selected with the `templates` option on the official plugins. That Kotlin module is the shape to copy. The generated Quarkus application is Java.

Before using an `io.zenwave360.sdk` type, method, or model key, read it in `../zenwave-sdk`.

Generated output is Java. Business and application code stays imperative. Templates write sources into an existing project.
