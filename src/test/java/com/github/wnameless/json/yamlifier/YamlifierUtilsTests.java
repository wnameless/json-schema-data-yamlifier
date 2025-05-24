package com.github.wnameless.json.yamlifier;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.yamlifier.YamlifierUtils.YamlifierMode;
import com.google.common.io.Resources;

class YamlifierUtilsTests {

  private static final int totalTestTypeNum = 4;
  private static final int totalAITestTypeNum = 3;

  private static final ObjectMapper jsonMapper = new ObjectMapper();

  @Test
  void testToYamlWithCommentsAndAI() throws IOException {
    for (int i = 1; i <= totalAITestTypeNum; i++) {
      var jsonSchema = Resources.toString(Resources.getResource("test-ai-" + i + "-schema.json"),
          StandardCharsets.UTF_8);
      var jsonData = Resources.toString(Resources.getResource("test-ai-" + i + "-data.json"),
          StandardCharsets.UTF_8);
      var out = Resources.toString(Resources.getResource("test-ai-" + i + "-out.yml"),
          StandardCharsets.UTF_8);
      var yaml = YamlifierUtils.toYamlWithComments(jsonSchema, jsonData);
      assertEquals(out, yaml);
    }
  }

  @Test
  void testToYamlWithCommentsAndAIAndMode() throws IOException {
    for (int i = 1; i <= totalAITestTypeNum; i++) {
      var jsonSchema = Resources.toString(Resources.getResource("test-ai-" + i + "-schema.json"),
          StandardCharsets.UTF_8);
      var jsonData = Resources.toString(Resources.getResource("test-ai-" + i + "-data.json"),
          StandardCharsets.UTF_8);
      var out = Resources.toString(Resources.getResource("test-ai-" + i + "-out-2.yml"),
          StandardCharsets.UTF_8);
      var yaml = YamlifierUtils.toYamlWithComments(jsonSchema, jsonData,
          YamlifierMode.TITLE_WITH_DESCRIPTION);
      assertEquals(out, yaml);
    }
  }

  @Test
  void testToYamlWithCommentsAndStringInputs() throws IOException {
    for (int i = 1; i <= totalTestTypeNum; i++) {
      var jsonSchema = Resources.toString(Resources.getResource("test-" + i + "-schema.json"),
          StandardCharsets.UTF_8);
      var jsonData = Resources.toString(Resources.getResource("test-" + i + "-data.json"),
          StandardCharsets.UTF_8);
      var out = Resources.toString(Resources.getResource("test-" + i + "-out.yml"),
          StandardCharsets.UTF_8);
      var yaml = YamlifierUtils.toYamlWithComments(jsonSchema, jsonData);
      assertEquals(out, yaml);
    }
  }

  @Test
  void testToYamlWithCommentsAndMapInputs() throws IOException {
    for (int i = 1; i <= totalTestTypeNum; i++) {
      var jsonSchema = Resources.toString(Resources.getResource("test-" + i + "-schema.json"),
          StandardCharsets.UTF_8);
      var jsonData = Resources.toString(Resources.getResource("test-" + i + "-data.json"),
          StandardCharsets.UTF_8);
      var out = Resources.toString(Resources.getResource("test-" + i + "-out.yml"),
          StandardCharsets.UTF_8);
      var yaml = YamlifierUtils.toYamlWithComments(
          jsonMapper.readValue(jsonSchema, new TypeReference<Map<String, Object>>() {}),
          jsonMapper.readValue(jsonData, Object.class));
      assertEquals(out, yaml);
    }
  }

  @Test
  void testToYamlWithCommentsAndStringInputsAndMode() throws IOException {
    for (int i = 1; i <= totalTestTypeNum; i++) {
      var jsonSchema = Resources.toString(Resources.getResource("test-" + i + "-schema.json"),
          StandardCharsets.UTF_8);
      var jsonData = Resources.toString(Resources.getResource("test-" + i + "-data.json"),
          StandardCharsets.UTF_8);
      var out = Resources.toString(Resources.getResource("test-" + i + "-out-2.yml"),
          StandardCharsets.UTF_8);
      var yaml = YamlifierUtils.toYamlWithComments(jsonSchema, jsonData,
          YamlifierMode.TITLE_WITH_DESCRIPTION);
      assertEquals(out, yaml);
    }
  }

  @Test
  void testToYamlWithCommentsAndMapInputsAndMode() throws IOException {
    for (int i = 1; i <= totalTestTypeNum; i++) {
      var jsonSchema = Resources.toString(Resources.getResource("test-" + i + "-schema.json"),
          StandardCharsets.UTF_8);
      var jsonData = Resources.toString(Resources.getResource("test-" + i + "-data.json"),
          StandardCharsets.UTF_8);
      var out = Resources.toString(Resources.getResource("test-" + i + "-out-2.yml"),
          StandardCharsets.UTF_8);
      var yaml = YamlifierUtils.toYamlWithComments(
          jsonMapper.readValue(jsonSchema, new TypeReference<Map<String, Object>>() {}),
          jsonMapper.readValue(jsonData, Object.class), YamlifierMode.TITLE_WITH_DESCRIPTION);
      assertEquals(out, yaml);
    }
  }

}
