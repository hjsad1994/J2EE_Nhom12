package nhom12.example.nhom12.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nhom12.example.nhom12.dto.request.CreateOrderRequest;
import nhom12.example.nhom12.dto.response.ApiResponse;
import nhom12.example.nhom12.dto.response.OrderResponse;
import nhom12.example.nhom12.exception.ResourceNotFoundException;
import nhom12.example.nhom12.model.User;
import nhom12.example.nhom12.model.enums.OrderStatus;
import nhom12.example.nhom12.repository.UserRepository;
import nhom12.example.nhom12.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;
  private final UserRepository userRepository;

  @PostMapping
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
      @AuthenticationPrincipal UserDetails principal,
      @Valid @RequestBody CreateOrderRequest request) {
    String userId = resolveUserId(principal);
    OrderResponse order = orderService.createOrder(userId, request);
    return new ResponseEntity<>(
        ApiResponse.created(order, "Order created successfully"), HttpStatus.CREATED);
  }

  @GetMapping("/my")
  public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
      @AuthenticationPrincipal UserDetails principal) {
    String userId = resolveUserId(principal);
    return ResponseEntity.ok(
        ApiResponse.success(orderService.getMyOrders(userId), "Orders retrieved successfully"));
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(
        ApiResponse.success(
            orderService.getAllOrders(PageRequest.of(page, size)),
            "Orders retrieved successfully"));
  }

  @PatchMapping("/{id}/status")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
      @PathVariable String id, @RequestParam OrderStatus status) {
    return ResponseEntity.ok(
        ApiResponse.success(orderService.updateStatus(id, status), "Status updated successfully"));
  }

  @PatchMapping("/{id}/cancel")
  public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
      @PathVariable String id,
      @RequestParam(required = false, defaultValue = "Không có lý do") String reason,
      @AuthenticationPrincipal UserDetails principal) {
    String userId = resolveUserId(principal);
    return ResponseEntity.ok(
        ApiResponse.success(
            orderService.cancelOrder(id, userId, reason), "Order cancelled successfully"));
  }

  private String resolveUserId(UserDetails principal) {
    User user =
        userRepository
            .findByUsername(principal.getUsername())
            .orElseThrow(
                () -> new ResourceNotFoundException("User", "username", principal.getUsername()));
    return user.getId();
  }
}
