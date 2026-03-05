package nhom12.example.nhom12.service;

import nhom12.example.nhom12.dto.request.CreateProductRequest;
import nhom12.example.nhom12.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

  ProductResponse createProduct(CreateProductRequest request);

  ProductResponse getProductById(String id);

  Page<ProductResponse> getAllProducts(Pageable pageable, String categoryId);

  ProductResponse updateProduct(String id, CreateProductRequest request);

  void deleteProduct(String id);
}
