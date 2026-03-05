package nhom12.example.nhom12.service.impl;

import lombok.RequiredArgsConstructor;
import nhom12.example.nhom12.dto.request.CreateProductRequest;
import nhom12.example.nhom12.dto.response.ProductResponse;
import nhom12.example.nhom12.exception.ResourceNotFoundException;
import nhom12.example.nhom12.mapper.ProductMapper;
import nhom12.example.nhom12.model.Category;
import nhom12.example.nhom12.model.Product;
import nhom12.example.nhom12.repository.CategoryRepository;
import nhom12.example.nhom12.repository.ProductRepository;
import nhom12.example.nhom12.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ProductMapper productMapper;

  @Override
  public ProductResponse createProduct(CreateProductRequest request) {
    Product product = productMapper.toEntity(request);
    return toResponse(productRepository.save(product));
  }

  @Override
  public ProductResponse getProductById(String id) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    return toResponse(product);
  }

  @Override
  public Page<ProductResponse> getAllProducts(Pageable pageable, String categoryId) {
    Page<Product> page =
        (categoryId != null && !categoryId.isBlank())
            ? productRepository.findByCategoryId(categoryId, pageable)
            : productRepository.findAll(pageable);
    return page.map(this::toResponse);
  }

  @Override
  public ProductResponse updateProduct(String id, CreateProductRequest request) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

    product.setName(request.getName());
    product.setBrand(request.getBrand());
    product.setCategoryId(request.getCategoryId());
    product.setPrice(request.getPrice());
    product.setOriginalPrice(request.getOriginalPrice());
    product.setImage(request.getImage());
    product.setRating(request.getRating() != null ? request.getRating() : product.getRating());
    product.setBadge(request.getBadge());
    product.setSpecs(request.getSpecs());

    return toResponse(productRepository.save(product));
  }

  @Override
  public void deleteProduct(String id) {
    if (!productRepository.existsById(id)) {
      throw new ResourceNotFoundException("Product", "id", id);
    }
    productRepository.deleteById(id);
  }

  private ProductResponse toResponse(Product product) {
    Category category =
        product.getCategoryId() != null
            ? categoryRepository.findById(product.getCategoryId()).orElse(null)
            : null;
    return productMapper.toResponse(product, category);
  }
}
