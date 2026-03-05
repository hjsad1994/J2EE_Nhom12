package nhom12.example.nhom12.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Product extends BaseDocument {

  @Indexed private String name;

  private String brand;
  private String categoryId;
  private double price;
  private Double originalPrice;
  private String image;
  private double rating;
  private String badge;
  private String specs;
}
