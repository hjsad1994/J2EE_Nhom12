package nhom12.example.nhom12.service.impl;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import nhom12.example.nhom12.dto.request.CreateCategoryRequest;
import nhom12.example.nhom12.dto.response.CategoryResponse;
import nhom12.example.nhom12.exception.BadRequestException;
import nhom12.example.nhom12.exception.DuplicateResourceException;
import nhom12.example.nhom12.exception.ResourceNotFoundException;
import nhom12.example.nhom12.model.Category;
import nhom12.example.nhom12.model.Product;
import nhom12.example.nhom12.repository.CategoryRepository;
import nhom12.example.nhom12.repository.ProductRepository;
import nhom12.example.nhom12.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

  private final CategoryRepository categoryRepository;
  private final ProductRepository productRepository;

  @Override
  public List<CategoryResponse> getAllCategories() {
    return categoryRepository.findAll().stream().map(this::toResponse).toList();
  }

  @Override
  public CategoryResponse getCategoryById(String id) {
    return toResponse(findById(id));
  }

  @Override
  public CategoryResponse createCategory(CreateCategoryRequest request) {
    if (categoryRepository.existsByName(request.getName())) {
      throw new DuplicateResourceException("Category", "name", request.getName());
    }
    Category category =
        Category.builder()
            .name(request.getName())
            .slug(toSlug(request.getName()))
            .description(request.getDescription())
            .icon(request.getIcon())
            .build();
    return toResponse(categoryRepository.save(category));
  }

  @Override
  public CategoryResponse updateCategory(String id, CreateCategoryRequest request) {
    Category category = findById(id);
    category.setName(request.getName());
    category.setSlug(toSlug(request.getName()));
    category.setDescription(request.getDescription());
    category.setIcon(request.getIcon());
    return toResponse(categoryRepository.save(category));
  }

  @Override
  public void deleteCategory(String id, boolean force) {
    if (!categoryRepository.existsById(id)) {
      throw new ResourceNotFoundException("Category", "id", id);
    }

    long productCount = productRepository.countByCategoryId(id);

    if (productCount > 0 && !force) {
      throw new BadRequestException(
          "Danh mục này đang có "
              + productCount
              + " sản phẩm. Sử dụng tùy chọn 'Xóa và gỡ liên kết' để xóa danh mục"
              + " và gỡ liên kết khỏi các sản phẩm.");
    }

    // Force delete: unlink all products from this category
    if (productCount > 0) {
      List<Product> products = productRepository.findByCategoryId(id);
      for (Product product : products) {
        product.setCategoryId(null);
        productRepository.save(product);
      }
    }

    categoryRepository.deleteById(id);
  }

  private Category findById(String id) {
    return categoryRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
  }

  private CategoryResponse toResponse(Category c) {
    return CategoryResponse.builder()
        .id(c.getId())
        .name(c.getName())
        .slug(c.getSlug())
        .description(c.getDescription())
        .icon(c.getIcon())
        .productCount(productRepository.countByCategoryId(c.getId()))
        .createdAt(c.getCreatedAt())
        .build();
  }

  private String toSlug(String name) {
    String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);
    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    return pattern
        .matcher(normalized)
        .replaceAll("")
        .toLowerCase()
        .replaceAll("[^a-z0-9\\s-]", "")
        .trim()
        .replaceAll("\\s+", "-");
  }
}
