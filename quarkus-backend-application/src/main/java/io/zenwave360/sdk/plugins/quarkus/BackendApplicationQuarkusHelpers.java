package io.zenwave360.sdk.plugins.quarkus;

import com.github.jknack.handlebars.Options;
import io.zenwave360.sdk.plugins.BackendApplicationDefaultGenerator;
import io.zenwave360.sdk.utils.JSONPath;
import io.zenwave360.sdk.zdl.utils.ZDLFindUtils;
import io.zenwave360.sdk.zdl.utils.ZDLJavaSignatureUtils;

import java.util.*;
import java.util.stream.Collectors;

public class BackendApplicationQuarkusHelpers {

    private final BackendApplicationDefaultGenerator generator;

    BackendApplicationQuarkusHelpers(BackendApplicationDefaultGenerator generator) {
        this.generator = generator;
    }

    public String javaType(Map field, Options options) {
        return ZDLJavaSignatureUtils.javaType(field);
    }

    public String naturalIdsRepoMethodSignature(Map entity, Options options) {
        return ZDLJavaSignatureUtils.naturalIdsRepoMethodSignature(entity);
    }

    public String findById(Map method, Options options) {
        var zdl = options.get("zdl");
        var hasNaturalId = JSONPath.get(method, "$.naturalId", false);
        if (hasNaturalId) {
            var entity = (Map) JSONPath.get(zdl, "$.allEntitiesAndEnums." + method.get("entity"));
            return ZDLJavaSignatureUtils.naturalIdsRepoMethodCallSignature(entity);
        }
        return "findById(id)";
    }

    /**
     * Finder call that yields the entity or null. Natural-id finders return {@code Optional},
     * so those calls are unwrapped with {@code orElse(null)}.
     */
    public String entityOrNull(Map method, Options options) {
        var call = findById(method, options);
        if (JSONPath.get(method, "$.naturalId", false)) {
            return call + ".orElse(null)";
        }
        return call;
    }

    /** Java expression of the id values, for concatenation after a message string. */
    public String idForStringInterpolation(Map method, Options options) {
        var zdl = options.get("zdl");
        var hasNaturalId = JSONPath.get(method, "$.naturalId", false);
        if (hasNaturalId) {
            var entity = (Map) JSONPath.get(zdl, "$.allEntitiesAndEnums." + method.get("entity"));
            var params = ZDLJavaSignatureUtils.naturalIdsRepoMethodCallSignature(entity)
                    .replaceAll("findBy[^(]*\\(([^)]*)\\)", "$1");
            return Arrays.stream(params.split(", "))
                    .collect(Collectors.joining(" + \", \" + "));
        }
        return "id";
    }

    /** Accepts both {@code {{methodParametersSignature "artifact.type" method}}} and the legacy {@code {{methodParametersSignature method}}}. */
    public String methodParametersSignature(Object artifactTypeOrMethod, Options options) {
        var zdl = (Map) options.get("zdl");
        var artifactType = artifactTypeOrMethod instanceof String type ? type : null;
        var method = (Map<String, Object>) (artifactType != null ? options.param(0) : artifactTypeOrMethod);
        return ZDLJavaSignatureUtils.methodParametersSignature(artifactType, generator.getIdJavaType(), method, zdl)
                .replace("Pageable pageable", "int page, int size");
    }

    public String methodParametersCallSignature(Map<String, Object> method, Options options) {
        var zdl = (Map) options.get("zdl");
        return ZDLJavaSignatureUtils.methodParametersCallSignature(method, zdl)
                .replace("pageable", "page, size");
    }

    public String logMethodCall(Map method, Options options) {
        var zdl = options.get("zdl");
        var methodName = (String) method.get("name");
        var parameters = ZDLJavaSignatureUtils.methodParametersCallSignature(method, (Map) zdl)
                .replace("pageable", "page, size");
        if (parameters.isEmpty()) {
            return String.format("log.debug(\"Request %s\");", methodName);
        }
        var parameterPlaceHolders = Arrays.stream(parameters.split(", "))
                .map(parameter -> "{}")
                .collect(Collectors.joining(" "));
        return String.format("log.debug(\"Request %s: %s\", %s);", methodName, parameterPlaceHolders, parameters);
    }

    public String nullableMethodParametersSignature(Object artifactTypeOrMethod, Options options) {
        return methodParametersSignature(artifactTypeOrMethod, options);
    }

    public String mapperInputSignature(String inputType, Options options) {
        var zdl = (Map) options.get("zdl");
        return ZDLJavaSignatureUtils.mapperInputSignature(inputType, zdl);
    }

    public String mapperInputAnnotations(String inputType, Options options) {
        return ZDLJavaSignatureUtils.mapperInputAnnotations(inputType, (Map) options.get("zdl"))
                .stream()
                .map(annotations -> String.format("@Mapping(source = \"%s\", target = \"%s\")", annotations.get("source"), annotations.get("target")))
                .collect(Collectors.joining("\n"));
    }

    /** Accepts both {@code {{returnType "artifact.type" method}}} and the legacy {@code {{returnType method}}} / {@code {{returnType}}}. */
    public String returnType(Object artifactTypeOrMethod, Options options) {
        var artifactType = artifactTypeOrMethod instanceof String type ? type : null;
        var method = (Map<String, Object>) (artifactType != null ? options.param(0) : artifactTypeOrMethod);
        return ZDLJavaSignatureUtils.methodReturnType(artifactType, method)
                .replaceAll("Page<([^>]+)>", "List<$1>")
                .replaceAll("\\bOptional<(.+)>", "$1");
    }

    public String fieldType(Map field, Options options) {
        String prefix = (String) options.hash.getOrDefault("prefix", "");
        String suffix = (String) options.hash.getOrDefault("suffix", "");
        return ZDLJavaSignatureUtils.fieldType(field, prefix, suffix);
    }

    public boolean isRequired(Map field, Options options) {
        var isRequired = JSONPath.get(field, "validations.required") != null;
        var isArray = JSONPath.get(field, "isArray", false);
        return isRequired && !isArray;
    }

    public String relationshipFieldType(Map field, Options options) {
        String prefix = (String) options.hash.getOrDefault("prefix", "");
        String suffix = (String) options.hash.getOrDefault("suffix", "");
        return ZDLJavaSignatureUtils.relationshipFieldType(field, prefix, suffix);
    }

    public String relationshipFieldTypeInitializer(Map field, Options options) {
        return ZDLJavaSignatureUtils.relationshipFieldTypeInitializer(field);
    }

    public String fieldTypeInitializer(Map field, Options options) {
        return ZDLJavaSignatureUtils.fieldTypeInitializer(field);
    }

    public String idFieldInitialization(Map method, Options options) {
        var zdl = options.get("zdl");
        var hasNaturalId = JSONPath.get(method, "$.naturalId", false);
        if (hasNaturalId) {
            var entity = (Map) JSONPath.get(zdl, "$.allEntitiesAndEnums." + method.get("entity"));
            List<Map> fields = ZDLFindUtils.naturalIdFields(entity);
            return fields.stream()
                    .map(field -> String.format("%s %s = %s;", field.get("type"), field.get("name"), ZDLJavaSignatureUtils.populateField(field)))
                    .collect(Collectors.joining("\n"));
        }
        return generator.getIdJavaType() + " id = " + idInitialization(generator.getIdJavaType()) + ";";
    }

    public String populateField(Map field, Options options) {
        return ZDLJavaSignatureUtils.populateField(field);
    }

    public String transitionNaturalIdExpression(Map entity, Options options) {
        if (entity == null) {
            return "null";
        }
        List<Map> fields = ZDLFindUtils.naturalIdFields(entity);
        if (fields == null || fields.isEmpty()) {
            return "null";
        }
        return fields.stream()
                .map(field -> "\"" + field.get("name") + "=\" + entity.get" + capitalize((String) field.get("name")) + "()")
                .collect(Collectors.joining(" + \", \" + "));
    }

    private static String capitalize(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private String idInitialization(String idJavaType) {
        return switch (idJavaType) {
            case "Long" -> "1L";
            case "Integer" -> "1";
            case "String" -> "\"1\"";
            default -> "";
        };
    }
}
