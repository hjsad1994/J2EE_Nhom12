package nhom12.example.nhom12.service.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nhom12.example.nhom12.dto.response.ProductResponse;
import nhom12.example.nhom12.exception.ResourceNotFoundException;
import nhom12.example.nhom12.mapper.ProductMapper;
import nhom12.example.nhom12.model.Category;
import nhom12.example.nhom12.model.Product;
import nhom12.example.nhom12.model.Wishlist;
import nhom12.example.nhom12.repository.CategoryRepository;
import nhom12.example.nhom12.repository.ProductRepository;
import nhom12.example.nhom12.repository.WishlistRepository;
import nhom12.example.nhom12.service.WishlistService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

  private final WishlistRepository wishlistRepository;
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ProductMapper productMapper;

  @Override
  public List<ProductResponse> getWishlist(String userId) {
    return toProductResponses(getOrCreate(userId).getProductIds());
  }

  @Override
  public List<ProductResponse> toggleProduct(String userId, String productId) {
    if (!productRepository.existsById(productId)) {
      throw new ResourceNotFoundException("Product", "id", productId);
    }

    Wishlist wishlist = getOrCreate(userId);
    if (wishlist.getProductIds().contains(productId)) {
      wishlist.getProductIds().remove(productId);
    } else {
      wishlist.getProductIds().add(productId);
    }

    wishlistRepository.save(wishlist);
    return toProductResponses(wishlist.getProductIds());
  }

  @Override
  public List<ProductResponse> clearWishlist(String userId) {
    Wishlist wishlist = getOrCreate(userId);
    wishlist.getProductIds().clear();
    wishlistRepository.save(wishlist);
    return List.of();
  }

  private Wishlist getOrCreate(String userId) {
    return wishlistRepository
        .findByUserId(userId)
        .orElseGet(
            () ->
                wishlistRepository.save(
                    Wishlist.builder().userId(userId).productIds(new ArrayList<>()).build()));
  }

  private List<ProductResponse> toProductResponses(List<String> productIds) {
    return productIds.stream()
        .map(id -> productRepository.findById(id).orElse(null))
        .filter(p -> p != null)
        .map(this::toResponse)
        .toList();
  }

  private ProductResponse toResponse(Product product) {
    Category category =
        product.getCategoryId() != null
            ? categoryRepository.findById(product.getCategoryId()).orElse(null)
            : null;
    return productMapper.toResponse(product, category);
  }
}
