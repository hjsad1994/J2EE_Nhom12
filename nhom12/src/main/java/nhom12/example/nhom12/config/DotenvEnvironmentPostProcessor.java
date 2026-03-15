package nhom12.example.nhom12.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class DotenvEnvironmentPostProcessor
    implements EnvironmentPostProcessor, Ordered {

  private static final String PROPERTY_SOURCE_NAME = "dotenv";

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, SpringApplication application) {
    Path dotenvPath = Path.of(".env");
    if (!Files.exists(dotenvPath)) {
      return;
    }

    Map<String, Object> properties = loadDotenv(dotenvPath);
    if (properties.isEmpty()) {
      return;
    }

    environment
        .getPropertySources()
        .addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  private Map<String, Object> loadDotenv(Path dotenvPath) {
    Map<String, Object> properties = new LinkedHashMap<>();
    try {
      List<String> lines = Files.readAllLines(dotenvPath, StandardCharsets.UTF_8);
      for (String rawLine : lines) {
        String line = rawLine.trim();
        if (line.isEmpty() || line.startsWith("#")) {
          continue;
        }

        int separatorIndex = line.indexOf('=');
        if (separatorIndex <= 0) {
          continue;
        }

        String key = line.substring(0, separatorIndex).trim();
        String value = line.substring(separatorIndex + 1).trim();
        if (value.length() >= 2
            && ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'")))) {
          value = value.substring(1, value.length() - 1);
        }

        properties.putIfAbsent(key, value);
      }
    } catch (IOException ignored) {
      return Map.of();
    }

    return properties;
  }
}
