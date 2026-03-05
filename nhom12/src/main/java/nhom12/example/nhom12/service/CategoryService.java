package nhom12.example.nhom12.service;

import java.util.List;
import nhom12.example.nhom12.dto.request.CreateCategoryRequest;
import nhom12.example.nhom12.dto.response.CategoryResponse;

public interface CategoryService {

  List<CategoryResponse> getAllCategories();

  CategoryResponse getCategoryById(String id);

  CategoryResponse createCategory(CreateCategoryRequest request);

  CategoryResponse updateCategory(String id, CreateCategoryRequest request);

  void deleteCategory(String id);
}
