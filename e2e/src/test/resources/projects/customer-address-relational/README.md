# customer-address-relational

Quarkus 3.40.1 scaffold for the shared customer-address relational model.

The pom imports the Quarkus BOM. That BOM manages Testcontainers 2.0.5. MapStruct 1.6.3 is pinned here because the BOM does not manage it. The generator writes Java sources into `src/main/java` and `src/test/java`. OpenAPI models are Java beans generated with openapi-generator 7.18.0 (`spring`) when `openapi.skip` is false. Optional fields use `jakarta.annotation.Nullable`.
