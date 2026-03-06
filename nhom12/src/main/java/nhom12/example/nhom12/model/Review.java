package nhom12.example.nhom12.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@CompoundIndex(def = "{'userId': 1, 'productId': 1}", unique = true)
public class Review extends BaseDocument {

  @Indexed private String productId;

  @Indexed private String userId;

  private String username;

  private int rating;

  private String comment;
}
