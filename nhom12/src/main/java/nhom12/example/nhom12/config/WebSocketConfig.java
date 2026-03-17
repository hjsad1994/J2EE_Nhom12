package nhom12.example.nhom12.config;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private static final String DEFAULT_ALLOWED_ORIGIN_PATTERNS =
      "http://localhost:5173,http://localhost:8080,http://localhost:*";

  private final Environment environment;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic", "/queue");
    config.setApplicationDestinationPrefixes("/app");
    config.setUserDestinationPrefix("/user");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    String[] allowedOriginPatterns =
        environment
            .getProperty("app.websocket.allowed-origin-patterns", DEFAULT_ALLOWED_ORIGIN_PATTERNS)
            .split(",");
    for (int i = 0; i < allowedOriginPatterns.length; i++) {
      allowedOriginPatterns[i] = allowedOriginPatterns[i].trim();
    }

    registry.addEndpoint("/ws").setAllowedOriginPatterns(allowedOriginPatterns);
  }
}
