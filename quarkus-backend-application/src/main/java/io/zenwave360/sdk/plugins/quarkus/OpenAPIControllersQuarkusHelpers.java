package io.zenwave360.sdk.plugins.quarkus;

import com.github.jknack.handlebars.Options;
import io.zenwave360.sdk.utils.JSONPath;
import io.zenwave360.sdk.zdl.utils.ZDLHttpUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.upperCase;

/**
 * Kotlin and JAX-RS helpers for {@link OpenAPIControllersQuarkusTemplates}.
 */
public class OpenAPIControllersQuarkusHelpers {

    public final String openApiModelNamePrefix;
    public final String openApiModelNameSuffix;

    public OpenAPIControllersQuarkusHelpers(String openApiModelNamePrefix, String openApiModelNameSuffix) {
        this.openApiModelNamePrefix = openApiModelNamePrefix;
        this.openApiModelNameSuffix = openApiModelNameSuffix;
    }

    public String jaxrsMethodParameters(Map operation, Options options) {
        var methodParams = ZDLHttpUtils.methodParameters(operation, openApiModelNamePrefix, openApiModelNameSuffix);
        Map<String, Map<String, Object>> rawByName = new HashMap<>();
        for (var raw : JSONPath.get(operation, "parameters", List.<Map<String, Object>>of())) {
            rawByName.put(JSONPath.get(raw, "name"), raw);
        }
        var parts = new ArrayList<String>();
        for (var param : methodParams) {
            String type = kotlinType(param.getKey());
            String name = param.getValue();
            Map<String, Object> raw = rawByName.get(name);
            if (raw == null) {
                parts.add("@Valid " + name + ": " + type);
                continue;
            }
            String in = JSONPath.get(raw, "in");
            Object defaultValue = JSONPath.get(raw, "schema.default");
            boolean required = JSONPath.get(raw, "required", false);
            var annotation = new StringBuilder();
            if ("path".equals(in)) {
                annotation.append("@PathParam(\"").append(name).append("\")");
            } else if ("header".equals(in)) {
                annotation.append("@HeaderParam(\"").append(name).append("\")");
            } else if ("query".equals(in)) {
                annotation.append("@QueryParam(\"").append(name).append("\")");
            }
            if (defaultValue != null) {
                if (annotation.length() > 0) {
                    annotation.append(' ');
                }
                annotation.append("@DefaultValue(\"").append(defaultValue).append("\")");
            }
            boolean nullable = !required && defaultValue == null && !"path".equals(in);
            parts.add((annotation.length() > 0 ? annotation + " " : "") + name + ": " + type + (nullable ? "?" : ""));
        }
        return String.join(", ", parts);
    }

    public String jaxrsReturnType(Map serviceOperation, Options options) {
        if (returnsResponse(serviceOperation, options)) {
            return "Response";
        }
        return kotlinType(String.valueOf(serviceOperation.get("responseEntityExpression")))
                .replace("void", "Unit")
                .replace("Void", "Unit");
    }

    public boolean returnsResponse(Map serviceOperation, Options options) {
        if (Boolean.TRUE.equals(serviceOperation.get("returnTypeIsOptional"))) {
            return true;
        }
        if (Boolean.TRUE.equals(serviceOperation.get("isBinaryDownload"))) {
            return true;
        }
        if (!"200".equals(String.valueOf(serviceOperation.get("statusCode")))) {
            return true;
        }
        return serviceOperation.get("responseSchemaName") == null;
    }

    public String serviceCall(Map serviceOperation, Options options) {
        var call = (String) serviceOperation.get("serviceMethodCall");
        if (call == null) {
            return "";
        }
        return call.replace("pageOf(page, limit, sort)", "page, limit");
    }

    public String httpPath(Map operation, Options options) {
        String path = JSONPath.get(operation, "x--path");
        return path == null ? "/" : path;
    }

    public String verbAnnotation(String httpMethod, Options options) {
        if (httpMethod == null || httpMethod.isBlank()) {
            return "";
        }
        return "@" + httpMethod.toUpperCase();
    }

    public String methodParameters(Map operation, Options options) {
        var requiredFields = JSONPath.get(operation, "$.requestBody.content..schema.required[*]", List.<String>of());
        var fieldsWitDefault = JSONPath.get(operation, "$.requestBody.content..schema.properties[*][?(@.default && !(@.default == 'null'))].x--property-name", List.of());
        var requiredParams = JSONPath.get(operation, "parameters[?(@.required == true)].name", List.<String>of());
        var paramsWithDefault = JSONPath.get(operation, "$.parameters[*][?(@.schema.default)].name", List.<String>of());
        var required = Stream.of(requiredFields, fieldsWitDefault, requiredParams, paramsWithDefault, List.of("reqBody", "input")).flatMap(List::stream).distinct().toList();
        return ZDLHttpUtils.methodParameters(operation, openApiModelNamePrefix, openApiModelNameSuffix).stream().map(param -> {
            return param.getValue() + ": " + kotlinType(param.getKey()) + (required.contains(param.getValue()) ? "" : "?");
        }).collect(Collectors.joining(", "));
    }

    public CharSequence asMethodParametersInitializer(Map<String, Object> operation, Options options) throws IOException {
        var openapi = (Map) options.get("openapi");
        var methodParams = ZDLHttpUtils.methodParameters(operation, openApiModelNamePrefix, openApiModelNameSuffix);
        if (methodParams.isEmpty()) {
            return "";
        }
        return methodParams.stream()
                .map(param -> "val " + param.getValue() + ": " + kotlinType(param.getKey()) + " = " + instantiateVar(openapi, param.getValue(), param.getKey()))
                .collect(Collectors.joining("\n"))
                .replace("Map = mutableMapOf", "Map<String, Any?> = mutableMapOf");
    }

    private String kotlinType(String type) {
        if (type == null) {
            return "";
        }
        return type.replace("Integer", "Int").replace("Map", "PatchMap");
    }

    private String instantiateVar(Map<String, Object> api, String varName, String type) {
        String kotlin = kotlinType(type);
        if ("Int".equals(kotlin) || "Integer".equals(type)) {
            return "0";
        }
        if ("Long".equals(kotlin)) {
            return "0L";
        }
        if ("String".equals(kotlin)) {
            return "\"\"";
        }
        if ("Boolean".equals(kotlin)) {
            return "false";
        }
        if ("Double".equals(kotlin)) {
            return "0.0";
        }
        if ("Float".equals(kotlin)) {
            return "0.0f";
        }
        if ("LocalDate".equals(kotlin)) {
            return "java.time.LocalDate.now()";
        }
        if (kotlin.startsWith("List<") || kotlin.startsWith("MutableList<")) {
            return "mutableListOf()";
        }
        if (kotlin.startsWith("Set<") || kotlin.startsWith("MutableSet<")) {
            return "mutableSetOf()";
        }
        if ("PatchMap".equals(kotlin) || kotlin.startsWith("PatchMap<") || kotlin.startsWith("Map<")) {
            return "mutableMapOf()";
        }
        if (kotlin.contains("MultipartFile")) {
            return "null";
        }
        if (kotlin.contains("ByteArray")) {
            return "byteArrayOf()";
        }
        if (kotlin.endsWith("DTO") || kotlin.endsWith(openApiModelNameSuffix)) {
            return kotlin + "(" + populateConstructor(api, type) + ")";
        }
        return "null";
    }

    private String populateConstructor(Map api, String type) {
        if (openApiModelNameSuffix == null || openApiModelNameSuffix.isEmpty()) {
            return "";
        }
        var dtoName = type.substring(openApiModelNamePrefix.length(), type.length() - openApiModelNameSuffix.length());
        var schema = JSONPath.get(api, "$.components.schemas." + dtoName);
        if (schema == null) {
            return "";
        }
        var required = JSONPath.get(schema, "$.required", List.of());
        var properties = JSONPath.get(schema, "$.properties", Map.<String, Map<String, Object>>of());
        return properties.entrySet().stream()
                .filter(entry -> required.contains(entry.getKey()))
                .map(entry -> entry.getKey() + " = " + populateProperty(entry.getValue()))
                .collect(Collectors.joining(", "));
    }

    private String populateProperty(Map property) {
        String type = (String) property.get("type");
        String format = (String) property.get("format");
        List<String> enums = (List) property.get("enum");
        if (enums != null && !enums.isEmpty()) {
            String otherEntity = (String) property.get("x--schema-name");
            if (otherEntity == null) {
                return "null";
            }
            return String.format("%s%s.%s", otherEntity, openApiModelNameSuffix, upperCase(enums.get(0)));
        }
        if ("date".equals(format)) {
            return "LocalDate.now()";
        }
        if ("date-time".equals(format)) {
            return "java.time.Instant.now()";
        }
        if ("integer".equals(type) && (StringUtils.isEmpty(format) || "int32".equals(format))) {
            return "1";
        }
        if ("integer".equals(type) && "int64".equals(format)) {
            return "1L";
        }
        if ("number".equals(type)) {
            return "java.math.BigDecimal.valueOf(0)";
        }
        if ("boolean".equals(type)) {
            return "true";
        }
        if ("array".equals(type)) {
            return "mutableListOf()";
        }
        if (property.get("x--schema-name") != null) {
            return property.get("x--schema-name") + openApiModelNameSuffix + "()";
        }
        return "\"aaa\"";
    }
}
