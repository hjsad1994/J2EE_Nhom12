package nhom12.example.nhom12.service;

import nhom12.example.nhom12.dto.request.CartItemRequest;
import nhom12.example.nhom12.dto.request.CartSyncRequest;
import nhom12.example.nhom12.dto.response.CartResponse;

public interface CartService {

  CartResponse getCart(String userId);

  CartResponse addItem(String userId, CartItemRequest request);

  CartResponse updateItemQuantity(
      String userId, String productId, String color, String storage, int quantity);

  CartResponse removeItem(String userId, String productId, String color, String storage);

  /**
   * Merge a guest cart (from localStorage) with the server cart on login. Items present only
   * locally are added to the server. Items present on both use the higher quantity (max 99).
   */
  CartResponse syncCart(String userId, CartSyncRequest request);

  void clearCart(String userId);
}
