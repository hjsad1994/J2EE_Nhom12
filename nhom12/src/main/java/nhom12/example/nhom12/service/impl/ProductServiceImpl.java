package nhom12.example.nhom12.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nhom12.example.nhom12.dto.request.CreateProductRequest;
import nhom12.example.nhom12.dto.response.ProductResponse;
import nhom12.example.nhom12.exception.BadRequestException;
import nhom12.example.nhom12.exception.ResourceNotFoundException;
import nhom12.example.nhom12.mapper.ProductMapper;
import nhom12.example.nhom12.model.Category;
import nhom12.example.nhom12.model.Order;
import nhom12.example.nhom12.model.Product;
import nhom12.example.nhom12.model.enums.OrderStatus;
import nhom12.example.nhom12.repository.CategoryRepository;
import nhom12.example.nhom12.repository.OrderRepository;
import nhom12.example.nhom12.repository.ProductRepository;
import nhom12.example.nhom12.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private static final List<OrderStatus> ACTIVE_STATUSES =
      List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.SHIPPING);

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final OrderRepository orderRepository;
  private final ProductMapper productMapper;

  /**
   * Creates a product with its initial stock atomically. The @Transactional annotation ensures
   * that if anything fails during product creation (e.g., validation, Cloudinary image save),
   * the partial state is rolled back and no incomplete product is persisted.
   */
  @Override
  @Transactional
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
    product.setStock(request.getStock() != null ? request.getStock() : product.getStock());
    product.setVariants(request.getVariants());

    return toResponse(productRepository.save(product));
  }

  @Override
  public void deleteProduct(String id) {
    if (!productRepository.existsById(id)) {
      throw new ResourceNotFoundException("Product", "id", id);
    }

    // Prevent deletion if product has active orders
    List<Order> activeOrders = orderRepository.findByProductIdAndStatusIn(id, ACTIVE_STATUSES);
    if (!activeOrders.isEmpty()) {
      throw new BadRequestException(
          "Không thể xóa sản phẩm này vì đang có "
              + activeOrders.size()
              + " đơn hàng chưa hoàn thành. Vui lòng xử lý các đơn hàng trước.");
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
