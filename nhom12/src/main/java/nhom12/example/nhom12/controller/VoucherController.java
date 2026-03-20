package nhom12.example.nhom12.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nhom12.example.nhom12.dto.request.CreateVoucherRequest;
import nhom12.example.nhom12.dto.request.UpdateVoucherRequest;
import nhom12.example.nhom12.dto.request.ValidateVoucherRequest;
import nhom12.example.nhom12.dto.response.ApiResponse;
import nhom12.example.nhom12.dto.response.VoucherResponse;
import nhom12.example.nhom12.dto.response.VoucherValidationResponse;
import nhom12.example.nhom12.service.VoucherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

  private final VoucherService voucherService;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<List<VoucherResponse>>> getAllVouchers() {
    return ResponseEntity.ok(
        ApiResponse.success(voucherService.getAllVouchers(), "Vouchers retrieved successfully"));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<VoucherResponse>> createVoucher(
      @Valid @RequestBody CreateVoucherRequest request) {
    return new ResponseEntity<>(
        ApiResponse.created(voucherService.createVoucher(request), "Voucher created successfully"),
        HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<VoucherResponse>> updateVoucher(
      @PathVariable String id, @Valid @RequestBody UpdateVoucherRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(voucherService.updateVoucher(id, request), "Voucher updated successfully"));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> deleteVoucher(@PathVariable String id) {
    voucherService.deleteVoucher(id);
    return ResponseEntity.ok(ApiResponse.success(null, "Voucher deleted successfully"));
  }

  @PostMapping("/validate")
  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  public ResponseEntity<ApiResponse<VoucherValidationResponse>> validateVouchers(
      @Valid @RequestBody ValidateVoucherRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            voucherService.validateOrderVouchers(
                request.getItems(), request.getProductVoucherCode(), request.getShippingVoucherCode()),
            "Voucher validated successfully"));
  }
}
