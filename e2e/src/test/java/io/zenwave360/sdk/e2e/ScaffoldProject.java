package io.zenwave360.sdk.e2e;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A checked-in archetype project from {@code src/test/resources/projects}, copied to {@code target/projects}.
 *
 * <p>The copy runs the project's own {@code zenwave} Maven profile (zenwave-sdk-maven-plugin + this repo's templates),
 * so the reactor modules must be installed first: {@code mvn install -De2e.tests.skip=false}.
 */
final class ScaffoldProject {

    private final Path dir;

    private ScaffoldProject(Path dir) {
        this.dir = dir;
    }

    /** Copies the project and replaces its zenwave-model.zdl with the shared model from test-resources. */
    static ScaffoldProject copy(String name, String classpathZdl) throws IOException {
        Path source = Path.of("src/test/resources/projects", name);
        Path target = Path.of("target/projects", name);
        deleteRecursively(target);
        try (Stream<Path> walk = Files.walk(source)) {
            for (Path path : walk.toList()) {
                Path relative = source.relativize(path);
                if (relative.getNameCount() > 0 && "target".equals(relative.getName(0).toString())) {
                    continue;
                }
                Path dest = target.resolve(relative.toString());
                if (Files.isDirectory(path)) {
                    Files.createDirectories(dest);
                } else {
                    Files.createDirectories(dest.getParent());
                    Files.copy(path, dest);
                }
            }
        }
        try (InputStream zdl = ScaffoldProject.class.getClassLoader().getResourceAsStream(classpathZdl)) {
            if (zdl == null) {
                throw new IllegalArgumentException("Not on classpath: " + classpathZdl);
            }
            Files.copy(zdl, target.resolve("zenwave-model.zdl"), StandardCopyOption.REPLACE_EXISTING);
        }
        return new ScaffoldProject(target);
    }

    /** Runs Maven on the copy. {@code generate} activates the {@code zenwave} profile. Extra args are {@code key=value}. */
    int mvn(boolean generate, String goal, String... properties) throws Exception {
        var request = new DefaultInvocationRequest();
        request.setBaseDirectory(dir.toFile());
        request.setPomFileName("pom.xml");
        request.setBatchMode(true);
        request.setGoals(List.of(goal));
        if (generate) {
            request.setProfiles(List.of("zenwave"));
        }
        var props = new Properties();
        for (String property : properties) {
            var kv = property.split("=", 2);
            props.setProperty(kv[0], kv[1]);
        }
        request.setProperties(props);
        return new DefaultInvoker().execute(request).getExitCode();
    }

    static void assertSuccess(int exitCode) {
        assertEquals(0, exitCode, "nested Maven build failed, see output above");
    }

    static boolean dockerAvailable() {
        try {
            var process = new ProcessBuilder("docker", "info").redirectErrorStream(true).start();
            process.getInputStream().transferTo(java.io.OutputStream.nullOutputStream());
            return process.waitFor(30, TimeUnit.SECONDS) && process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(root)) {
            for (Path path : walk.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }
}
