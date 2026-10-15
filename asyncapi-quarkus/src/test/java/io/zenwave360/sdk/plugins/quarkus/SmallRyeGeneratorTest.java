package io.zenwave360.sdk.plugins.quarkus;

import io.zenwave360.sdk.MainGenerator;
import io.zenwave360.sdk.Plugin;
import io.zenwave360.sdk.plugins.AsyncAPIGeneratorPlugin;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmallRyeGeneratorTest {

    @Test
    void generatesSmallRyeProducerAndConsumer() throws Exception {
        String targetFolder = "target/orders-async";
        Plugin plugin = new AsyncAPIGeneratorPlugin()
                .withApiFile("classpath:asyncapi/asyncapi.yml")
                .withTargetFolder(targetFolder)
                .withOption("templates", SmallRyeTemplates.class.getName())
                .withOption("modelPackage", "io.example.orders.model")
                .withOption("producerApiPackage", "io.example.orders.producer")
                .withOption("consumerApiPackage", "io.example.orders.consumer");

        new MainGenerator().generate(plugin);

        Path root = Path.of(targetFolder);
        String producer = Files.readString(root.resolve(
                "src/main/java/io/example/orders/producer/DefaultDefaultServiceEventsProducer.java"));
        String producerApi = Files.readString(root.resolve(
                "src/main/java/io/example/orders/producer/DefaultServiceEventsProducer.java"));
        String consumer = Files.readString(root.resolve(
                "src/main/java/io/example/orders/consumer/CreateCustomerChannelConsumer.java"));
        String consumerApi = Files.readString(root.resolve(
                "src/main/java/io/example/orders/consumer/ICreateCustomerChannelConsumerService.java"));
        String inMemory = Files.readString(root.resolve(
                "src/test/java/io/example/orders/producer/InMemoryDefaultServiceEventsProducer.java"));

        assertTrue(producer.contains("Emitter"));
        assertTrue(producer.contains("@Channel"));
        assertTrue(producer.contains("implements DefaultServiceEventsProducer"));
        assertTrue(consumer.contains("@Incoming"));
        assertTrue(consumer.contains("doCreateCustomer"));

        List<Path> emitterFiles = new ArrayList<>();
        List<Path> incomingFiles = new ArrayList<>();
        try (Stream<Path> files = Files.walk(root)) {
            files.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                try {
                    String source = Files.readString(path);
                    if (source.contains("Emitter")) {
                        emitterFiles.add(path);
                    }
                    if (source.contains("@Incoming")) {
                        incomingFiles.add(path);
                    }
                    assertFalse(source.contains("import org.springframework"), path.toString());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        assertEquals(List.of(root.resolve(
                "src/main/java/io/example/orders/producer/DefaultDefaultServiceEventsProducer.java")), emitterFiles);
        assertEquals(List.of(root.resolve(
                "src/main/java/io/example/orders/consumer/CreateCustomerChannelConsumer.java")), incomingFiles);
        assertFalse(producerApi.contains("Emitter") || producerApi.contains("@Incoming"));
        assertFalse(consumerApi.contains("Emitter") || consumerApi.contains("@Incoming"));
        assertFalse(inMemory.contains("Emitter") || inMemory.contains("@Incoming"));
        assertFalse(producer.contains("@Incoming"));
        assertFalse(consumer.contains("Emitter"));
    }
}
