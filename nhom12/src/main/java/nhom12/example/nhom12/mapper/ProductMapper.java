package nhom12.example.nhom12.mapper;

import nhom12.example.nhom12.dto.request.CreateProductRequest;
import nhom12.example.nhom12.dto.response.ProductResponse;
import nhom12.example.nhom12.model.Category;
import nhom12.example.nhom12.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

  public Product toEntity(CreateProductRequest request) {
    return Product.builder()
        .name(request.getName())
        .brand(request.getBrand())
        .categoryId(request.getCategoryId())
        .price(request.getPrice())
        .originalPrice(request.getOriginalPrice())
        .image(request.getImage())
        .rating(request.getRating() != null ? request.getRating() : 0.0)
        .badge(request.getBadge())
        .specs(request.getSpecs())
        .build();
  }

  public ProductResponse toResponse(Product product) {
    return toResponse(product, null);
  }

  public ProductResponse toResponse(Product product, Category category) {
    return ProductResponse.builder()
        .id(product.getId())
        .name(product.getName())
        .brand(product.getBrand())
        .categoryId(product.getCategoryId())
        .categoryName(category != null ? category.getName() : null)
        .price(product.getPrice())
        .originalPrice(product.getOriginalPrice())
        .image(product.getImage())
        .rating(product.getRating())
        .badge(product.getBadge())
        .specs(product.getSpecs())
        .createdAt(product.getCreatedAt())
        .updatedAt(product.getUpdatedAt())
        .build();
  }
}
