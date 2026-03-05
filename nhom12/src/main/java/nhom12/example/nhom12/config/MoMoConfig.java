package nhom12.example.nhom12.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConfigurationProperties(prefix = "momo")
@Getter
@Setter
public class MoMoConfig {

  private String partnerCode;
  private String accessKey;
  private String secretKey;
  private String apiUrl;
  private String ipnUrl;
  private String redirectUrl;

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
