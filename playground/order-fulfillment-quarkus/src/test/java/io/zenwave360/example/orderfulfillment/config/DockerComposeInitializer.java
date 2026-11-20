package io.zenwave360.example.orderfulfillment.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class DockerComposeInitializer {

    private static final Logger log = LoggerFactory.getLogger(DockerComposeInitializer.class);

    /**
     * Use this annotation to activate TestContainers in your test.
     */
    public @interface EnableDockerCompose {}

    private record Service(String name, int port, String envVar, String envValueTemplate) {}

    private static final String DOCKER_COMPOSE_FILE = "./docker-compose.yml";
    private static final List<Service> SERVICES = List.of(
            new Service("postgresql", 5432, "DATASOURCE_URL", "jdbc:postgresql://%s:%s/app"),
            new Service("kafka", 9092, "KAFKA_BOOTSTRAP_SERVERS", "%s:%s"));

    public static final String HOST = DockerClientFactory.instance().dockerHostIpAddress();
    public static final ComposeContainer container =
            new ComposeContainer(new File(DOCKER_COMPOSE_FILE)).withEnv("HOST", HOST);

    static {
        for (var service : SERVICES) {
            if ("schema-registry".equals(service.name())) {
                container.withExposedService(
                        service.name(),
                        service.port(),
                        Wait.forHttp("/subjects").forStatusCode(200));
            } else {
                container.withExposedService(service.name(), service.port(), Wait.forListeningPort());
            }
        }
    }

    private static boolean isContainerRunning = false;

    public void start() {
        if (isDockerComposeRunningAllServices(SERVICES)) {
            log.info("Docker Compose Containers are running from local docker-compose. Skipping TestContainers...");
        } else {
            log.info(
                    "Docker Compose Containers are not running from local docker-compose. Starting from TestContainers...");
            if (isContainerRunning) {
                log.info("Docker Compose Containers are already running from TestContainers. Skipping...");
            } else {
                log.info("Starting Docker Compose Containers from TestContainers...");
                container.start();
                isContainerRunning = true;

                for (var service : SERVICES) {
                    var port = container.getServicePort(service.name(), service.port());
                    log.info("DockerCompose exposed port for {}: {}", service.name(), HOST + ":" + port);
                    log.info("DockerCompose Service {} listening: {}", service.name(), isPortOpen(HOST, port));
                    if (service.envValueTemplate() != null) {
                        System.setProperty(service.envVar(), String.format(service.envValueTemplate(), HOST, port));
                    }
                }
            }
        }
    }

    private boolean isDockerComposeRunningAllServices(List<Service> services) {
        var serviceNames = new ArrayList<String>();
        for (var service : services) {
            serviceNames.add(service.name());
        }
        return Stream.of("docker-compose", "docker-compose.exe").anyMatch(cmd -> {
            try {
                return getDockerComposeRunningServices(cmd, "-f", DOCKER_COMPOSE_FILE, "ps", "--services")
                        .containsAll(serviceNames);
            } catch (IOException | InterruptedException e) {
                return false;
            }
        });
    }

    public List<String> getDockerComposeRunningServices(String... command) throws IOException, InterruptedException {
        var services = new ArrayList<String>();
        var process = new ProcessBuilder(command).start();
        try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    services.add(line);
                }
            }
        }
        process.waitFor();
        return services;
    }

    public boolean isPortOpen(String host, int port) {
        try (var socket = new Socket(host, port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
