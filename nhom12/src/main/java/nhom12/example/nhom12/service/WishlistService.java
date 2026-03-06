package nhom12.example.nhom12.service;

import java.util.List;
import nhom12.example.nhom12.dto.response.ProductResponse;

public interface WishlistService {
  List<ProductResponse> getWishlist(String userId);

  List<ProductResponse> toggleProduct(String userId, String productId);

  List<ProductResponse> clearWishlist(String userId);
}
