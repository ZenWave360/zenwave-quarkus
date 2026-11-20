# Customer Address Relational (Quarkus)

E2E project: the archetype scaffold with no generated sources checked in.
The e2e test generates everything from `test-resources/src/main/resources/zdl/customer-address-relational.zdl`
(CleanHexagonalProjectLayout, `io.example.orders`), compiles, and runs `ArchitectureTest`.

Scaffold origin and conventions: [`docs/e2e-scaffold.md`](../../../../../../docs/e2e-scaffold.md).

```bash
mvn install                                   # repo root: templates
./mvnw -Pzenwave test -Dtest=ArchitectureTest -Dsurefire.failIfNoSpecifiedTests=false
```

Service and HTTP tests are generated as TODO skeletons. No Flyway migration is checked in, so the prod profile does not start until you add `db/migration/V1__init.sql`.
