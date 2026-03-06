package nhom12.example.nhom12.model;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "wishlists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Wishlist extends BaseDocument {

  @Indexed(unique = true)
  private String userId;

  @lombok.Builder.Default private List<String> productIds = new ArrayList<>();
}
