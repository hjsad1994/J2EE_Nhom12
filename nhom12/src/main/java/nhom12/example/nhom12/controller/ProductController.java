package nhom12.example.nhom12.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import nhom12.example.nhom12.dto.request.CreateProductRequest;
import nhom12.example.nhom12.dto.response.ApiResponse;
import nhom12.example.nhom12.dto.response.ProductResponse;
import nhom12.example.nhom12.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

  private final ProductService productService;

  @GetMapping
  public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
      @RequestParam(required = false) String categoryId) {
    Page<ProductResponse> products =
        productService.getAllProducts(PageRequest.of(page, size), categoryId);
    return ResponseEntity.ok(ApiResponse.success(products, "Products retrieved successfully"));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable String id) {
    ProductResponse product = productService.getProductById(id);
    return ResponseEntity.ok(ApiResponse.success(product, "Product retrieved successfully"));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
      @Valid @RequestBody CreateProductRequest request) {
    ProductResponse product = productService.createProduct(request);
    return new ResponseEntity<>(
        ApiResponse.created(product, "Product created successfully"), HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
      @PathVariable String id, @Valid @RequestBody CreateProductRequest request) {
    ProductResponse product = productService.updateProduct(id, request);
    return ResponseEntity.ok(ApiResponse.success(product, "Product updated successfully"));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String id) {
    productService.deleteProduct(id);
    return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
  }
}
