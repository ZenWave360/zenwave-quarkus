package io.zenwave360.sdk.plugins.quarkus;

import io.zenwave360.sdk.generators.AbstractAsyncapiGenerator;
import io.zenwave360.sdk.plugins.AsyncAPIGenerator;
import io.zenwave360.sdk.plugins.templates.AsyncAPIHandlebarsHelpers;
import io.zenwave360.sdk.templating.HandlebarsEngine;

/**
 * Quarkus templates for {@code AsyncAPIGeneratorPlugin}, selected by class name.
 *
 * <p>Same six target files as {@code SpringKafkaTemplates} when {@code transactionalOutbox} is {@code none}.
 * Shared templates stay on the SDK folder. The producer, in-memory producer, and consumer live under {@code smallrye/}.
 *
 * <p>Extra option {@code producerImplementationPackage}: package of the SmallRye producer implementation
 * (e.g. {@code infrastructure.events} in a hexagonal layout). Defaults to {@code producerApiPackage}.
 */
public class SmallRyeTemplates extends AbstractAsyncapiGenerator.Templates {

    public SmallRyeTemplates(AsyncAPIGenerator generator) {
        super("io/zenwave360/sdk/plugins/AsyncAPIGenerator");

        HandlebarsEngine handlebarsEngine = (HandlebarsEngine) generator.getTemplateEngine();
        handlebarsEngine.registerHelpers(new AsyncAPIHandlebarsHelpers(generator));

        addTemplate(producerTemplates, "shared/producer/mocks/EventsProducerInMemoryContext.java",
                "src/test/java/{{asPackageFolder producerApiPackage}}/EventsProducerInMemoryContext.java");

        addTemplate(producerByServiceTemplates, "shared/producer/IProducer.java",
                "src/main/java/{{asPackageFolder producerApiPackage}}/{{producerInterfaceName serviceName operationRoleType}}.java");
        addTemplate(producerByServiceTemplates, "smallrye/producer/Producer.java",
                "src/main/java/{{#if producerImplementationPackage}}{{asPackageFolder producerImplementationPackage}}{{else}}{{asPackageFolder producerApiPackage}}{{/if}}/{{producerClassName serviceName operationRoleType}}.java");
        addTemplate(producerByServiceTemplates, "smallrye/producer/InMemoryEventsProducer.java",
                "src/test/java/{{asPackageFolder producerApiPackage}}/{{producerInMemoryName serviceName operationRoleType}}.java");

        addTemplate(consumerByChannelTemplates, "shared/consumer/{{style}}/IServiceByChannel.java",
                "src/main/java/{{asPackageFolder consumerApiPackage}}/{{consumerServiceInterfaceName (camelCase channelName)}}.java");
        addTemplate(consumerByChannelTemplates, "smallrye/consumer/{{style}}/Consumer.java",
                "src/main/java/{{asPackageFolder consumerApiPackage}}/{{consumerName (camelCase channelName)}}.java");
    }
}
