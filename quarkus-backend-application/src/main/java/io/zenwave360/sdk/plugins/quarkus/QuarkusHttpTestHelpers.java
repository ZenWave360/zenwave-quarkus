package io.zenwave360.sdk.plugins.quarkus;

import com.github.jknack.handlebars.Options;

import java.util.Map;

/**
 * Kotlin object literals for Quarkus HTTP tests. JSON examples stay on {@code SpringWebTestClientHelpers}.
 */
public class QuarkusHttpTestHelpers {

    private final String openApiModelNamePrefix;
    private final String openApiModelNameSuffix;

    public QuarkusHttpTestHelpers(String openApiModelNamePrefix, String openApiModelNameSuffix) {
        this.openApiModelNamePrefix = openApiModelNamePrefix;
        this.openApiModelNameSuffix = openApiModelNameSuffix;
    }

    public String newPropertyObject(Map<String, Object> property, Options options) {
        boolean isArray = "array".equals(property.get("type"));
        boolean isObject = "object".equals(property.get("type"));
        String schemaName = (String) property.get("x--schema-name");
        if (isArray) {
            return "mutableListOf()";
        }
        if (isObject) {
            return openApiModelNamePrefix + schemaName + openApiModelNameSuffix + "()";
        }
        return "null";
    }
}
