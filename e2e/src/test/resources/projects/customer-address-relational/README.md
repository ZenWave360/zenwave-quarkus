# Customer Address Relational (Quarkus)

E2E project: the archetype scaffold with no generated sources checked in.
The e2e test generates everything from `test-resources/src/main/resources/zdl/customer-address-relational.zdl`
(CleanHexagonalProjectLayout, `io.example.orders`), compiles, runs `ArchitectureTest` and, with Docker, `CustomerCommandsKafkaTest`.

Scaffold origin and conventions: [`docs/e2e-scaffold.md`](../../../../../../docs/e2e-scaffold.md).

```bash
mvn install                                   # repo root: templates
./mvnw -Pzenwave test -Dtest=ArchitectureTest,CustomerCommandsKafkaTest -Dsurefire.failIfNoSpecifiedTests=false
```

Hand-written (the only checked-in sources):

- `adapters/commands/CustomerCommandsService` + `mappers/CustomerCommandsMapper`: implement the generated `ICustomerCommandsChannelConsumerService`. ZenWave does not generate it for a provider-side command channel, and without it ArC cannot boot.
- `CustomerCommandsKafkaTest` (Dev Services): a poison command lands in `dead-letter-topic-customerCommandsChannel`, a valid one creates the customer, and the event is published after commit with `__TypeId__`.

`application.properties`: `failure-strategy=dead-letter-queue` on the command channel. `pom.xml`: `producerImplementationPackage` = `infrastructure.events`.

Service and HTTP tests are generated as TODO skeletons. No Flyway migration is checked in, so the prod profile does not start until you add `db/migration/V1__init.sql`.
