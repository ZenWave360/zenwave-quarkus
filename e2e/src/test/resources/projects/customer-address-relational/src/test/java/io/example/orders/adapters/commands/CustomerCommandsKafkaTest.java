package io.example.orders.adapters.commands;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Boots the app on Dev Services (Postgres, Kafka) and drives it through Kafka only:
 * a poison command goes to the dead-letter topic, the next command creates a customer,
 * and CustomerCreated is published after commit with a {@code __TypeId__} header.
 */
@QuarkusTest
class CustomerCommandsKafkaTest {

    static final Duration TIMEOUT = Duration.ofSeconds(30);

    @ConfigProperty(name = "kafka.bootstrap.servers")
    String bootstrapServers;

    @Inject
    EntityManager entityManager;

    @Test
    void createCustomerCommandRoundTrip() throws Exception {
        String username = "kafka-" + UUID.randomUUID();
        try (var events = consumer("customer-events");
                var deadLetters = consumer("dead-letter-topic-customerCommandsChannel");
                var producer = producer()) {
            producer.send(new ProducerRecord<>("customer-commands", "{not json")).get();
            producer.send(new ProducerRecord<>("customer-commands",
                    "{\"username\":\"" + username + "\",\"email\":\"" + username + "@example.com\",\"address\":[]}")).get();

            Assertions.assertFalse(poll(deadLetters, r -> r.value().startsWith("{not json")).isEmpty(), "poison message in DLQ");
            Assertions.assertTrue(waitFor(() -> countCustomers(username) == 1), "customer created from command");

            var created = poll(events, r -> r.value().contains(username));
            Assertions.assertFalse(created.isEmpty(), "CustomerCreated published");
            var typeId = created.get(0).headers().lastHeader("__TypeId__");
            Assertions.assertNotNull(typeId);
            var typeName = new String(typeId.value(), StandardCharsets.UTF_8);
            Assertions.assertTrue(typeName.startsWith("io.example.orders.core.outbound.events.dtos."), typeName);
        }
    }

    long countCustomers(String username) {
        return QuarkusTransaction.requiringNew().call(() -> entityManager
                .createQuery("select count(c) from Customer c where c.username = :username", Long.class)
                .setParameter("username", username)
                .getSingleResult());
    }

    KafkaProducer<String, String> producer() {
        return new KafkaProducer<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName(),
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName()));
    }

    KafkaConsumer<String, String> consumer(String topic) {
        var consumer = new KafkaConsumer<String, String>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName()));
        consumer.subscribe(List.of(topic));
        return consumer;
    }

    static List<ConsumerRecord<String, String>> poll(KafkaConsumer<String, String> consumer,
            java.util.function.Predicate<ConsumerRecord<String, String>> match) {
        var found = new ArrayList<ConsumerRecord<String, String>>();
        var deadline = Instant.now().plus(TIMEOUT);
        while (found.isEmpty() && Instant.now().isBefore(deadline)) {
            for (var record : consumer.poll(Duration.ofMillis(500))) {
                if (record.value() != null && match.test(record)) {
                    found.add(record);
                }
            }
        }
        return found;
    }

    static boolean waitFor(java.util.function.BooleanSupplier condition) throws InterruptedException {
        var deadline = Instant.now().plus(TIMEOUT);
        while (Instant.now().isBefore(deadline)) {
            if (condition.getAsBoolean()) {
                return true;
            }
            Thread.sleep(250);
        }
        return false;
    }
}
