package com.github.wnameless.json.yamlifier;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class YamlifierUtils {

  private YamlifierUtils() {}

  public enum YamlifierMode {
    TITLE, TITLE_WITH_DESCRIPTION
  }

  private static final ObjectMapper jsonMapper = new ObjectMapper();

  public static String toYamlWithComments(Map<String, Object> jsonSchema, Object jsonData)
      throws IOException {
    return toYamlWithComments(jsonMapper.convertValue(jsonSchema, JsonNode.class),
        jsonMapper.convertValue(jsonData, JsonNode.class));
  }

  public static String toYamlWithComments(Map<String, Object> jsonSchema, Object jsonData,
      YamlifierMode mode) throws IOException {
    return toYamlWithComments(jsonMapper.convertValue(jsonSchema, JsonNode.class),
        jsonMapper.convertValue(jsonData, JsonNode.class), mode);
  }

  public static String toYamlWithComments(String jsonSchema, String jsonData) throws IOException {
    return toYamlWithComments(jsonMapper.readTree(jsonSchema), jsonMapper.readTree(jsonData));
  }

  public static String toYamlWithComments(String jsonSchema, String jsonData, YamlifierMode mode)
      throws IOException {
    return toYamlWithComments(jsonMapper.readTree(jsonSchema), jsonMapper.readTree(jsonData), mode);
  }

  public static String toYamlWithComments(JsonNode schemaNode, JsonNode dataNode,
      YamlifierMode mode) throws IOException {
    StringBuilder yamlWithComments = new StringBuilder("---\n");
    // Print root-level title/description if present
    if (schemaNode.has("title")) {
      appendTitleAndDescription(yamlWithComments, schemaNode, "", mode);
    }
    processRoot(yamlWithComments, schemaNode, dataNode, "", mode);
    return yamlWithComments.toString();
  }

  // Overload for backward compatibility (TITLE mode as default)
  public static String toYamlWithComments(JsonNode schemaNode, JsonNode dataNode)
      throws IOException {
    return toYamlWithComments(schemaNode, dataNode, YamlifierMode.TITLE);
  }

  // --- Pass mode recursively ---
  private static void processRoot(StringBuilder yaml, JsonNode schema, JsonNode data, String indent,
      YamlifierMode mode) {
    if (schema == null || data == null) return;
    String type = schema.has("type") ? schema.get("type").asText() : null;

    if ("array".equals(type) && data.isArray()) {
      addYamlArrayWithComments(yaml, schema, data, indent, mode);
    } else if ("object".equals(type) && data.isObject()) {
      addYamlWithComments(yaml, schema, data, indent, mode);
    } else {
      appendEnumComment(yaml, schema, data, indent, mode);
    }
  }

  private static void addYamlWithComments(StringBuilder yaml, JsonNode schemaNode,
      JsonNode dataNode, String indent, YamlifierMode mode) {
    if (schemaNode == null || dataNode == null) return;
    Iterator<Map.Entry<String, JsonNode>> fields = dataNode.properties().iterator();
    while (fields.hasNext()) {
      Map.Entry<String, JsonNode> field = fields.next();
      String fieldName = field.getKey();
      JsonNode value = field.getValue();

      JsonNode propertySchema = findSchemaForProperty(schemaNode, fieldName);

      if (propertySchema != null && propertySchema.has("title")) {
        appendTitleAndDescription(yaml, propertySchema, indent, mode);
      }
      String nextIndent = indent + "  ";

      if (value.isObject()) {
        yaml.append(indent).append(fieldName).append(":\n");
        addYamlWithComments(yaml, propertySchema, value, nextIndent, mode);
      } else if (value.isArray()) {
        yaml.append(indent).append(fieldName).append(":\n");
        addYamlArrayWithComments(yaml, propertySchema, value, nextIndent, mode);
      } else {
        appendEnumComment(yaml, propertySchema, value, indent + fieldName + ": ", mode);
      }
    }
  }

  private static void addYamlArrayWithComments(StringBuilder yaml, JsonNode schemaNode,
      JsonNode dataArray, String indent, YamlifierMode mode) {
    if (schemaNode == null || dataArray == null || !dataArray.isArray()) return;

    JsonNode itemsSchema = schemaNode.get("items");
    boolean isTuple = itemsSchema != null && itemsSchema.isArray();

    for (int idx = 0; idx < dataArray.size(); idx++) {
      JsonNode item = dataArray.get(idx);

      JsonNode itemSchema = null;
      if (isTuple) {
        if (idx < itemsSchema.size()) {
          itemSchema = itemsSchema.get(idx);
        }
      } else {
        itemSchema = itemsSchema;
      }

      // Print tuple element title/description as comment
      if (itemSchema != null && itemSchema.has("title")) {
        appendTitleAndDescription(yaml, itemSchema, indent, mode);
      }

      if (item.isObject()) {
        yaml.append(indent).append("-\n");
        addYamlWithComments(yaml, itemSchema, item, indent + "  ", mode);
      } else if (item.isArray()) {
        yaml.append(indent).append("-\n");
        addYamlArrayWithComments(yaml, itemSchema, item, indent + "  ", mode);
      } else {
        appendEnumComment(yaml, itemSchema, item, indent + "- ", mode);
      }
    }
  }

  // Append inline enum comment
  private static void appendEnumComment(StringBuilder yaml, JsonNode schema, JsonNode value,
      String prefix, YamlifierMode mode) {
    boolean enumHandled = false;
    if (schema != null && schema.has("enum") && schema.has("enumNames")) {
      JsonNode enumValues = schema.get("enum");
      JsonNode enumNames = schema.get("enumNames");
      for (int i = 0; i < enumValues.size(); i++) {
        if (enumValues.get(i).asText().equals(value.asText())) {
          yaml.append(prefix).append(value.asText()).append(" # ").append(enumNames.get(i).asText())
              .append("\n");
          enumHandled = true;
          break;
        }
      }
    }
    if (!enumHandled) {
      yaml.append(prefix).append(value.asText()).append("\n");
    }
  }

  // Print title (and optionally description) as comments
  private static void appendTitleAndDescription(StringBuilder yaml, JsonNode schema, String indent,
      YamlifierMode mode) {
    if (schema.has("title")) {
      yaml.append(indent).append("# ").append(schema.get("title").asText()).append("\n");
    }
    if (mode == YamlifierMode.TITLE_WITH_DESCRIPTION && schema.has("description")) {
      yaml.append(indent).append("# ").append(schema.get("description").asText()).append("\n");
    }
  }

  // -- existing findSchemaForProperty unchanged --
  private static JsonNode findSchemaForProperty(JsonNode schemaNode, String propertyName) {
    if (schemaNode == null) return null;
    JsonNode directProperties = schemaNode.get("properties");
    if (directProperties != null && directProperties.has(propertyName)) {
      return directProperties.get(propertyName);
    }
    String[] combinators = {"anyOf", "oneOf", "allOf"};
    for (String combinator : combinators) {
      JsonNode combinedSchemas = schemaNode.get(combinator);
      if (combinedSchemas != null && combinedSchemas.isArray()) {
        for (JsonNode subSchema : combinedSchemas) {
          JsonNode result = findSchemaForProperty(subSchema, propertyName);
          if (result != null) return result;
        }
      }
    }
    if (schemaNode.has("items")) {
      return findSchemaForProperty(schemaNode.get("items"), propertyName);
    }
    return null;
  }

}
