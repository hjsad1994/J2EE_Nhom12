package nhom12.example.nhom12.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Sent by the frontend on login to merge the guest cart (localStorage) with the server cart. The
 * server takes the union: server items are kept, and local items not present on the server are
 * added. If an item exists on both, the higher quantity wins (capped at 99).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartSyncRequest {
  private List<CartItemRequest> items;
}
