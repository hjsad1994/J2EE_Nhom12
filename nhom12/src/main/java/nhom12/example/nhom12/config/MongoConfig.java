package nhom12.example.nhom12.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
public class MongoConfig extends AbstractMongoClientConfiguration {

  @Value("${spring.data.mongodb.uri}")
  private String mongoUri;

  @Override
  protected String getDatabaseName() {
    return "nhom12";
  }

  @Override
  public MongoClient mongoClient() {
    ConnectionString connectionString = new ConnectionString(mongoUri);
    MongoClientSettings settings =
        MongoClientSettings.builder().applyConnectionString(connectionString).build();
    return MongoClients.create(settings);
  }
}
