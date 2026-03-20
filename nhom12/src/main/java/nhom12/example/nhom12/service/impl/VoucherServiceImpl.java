package nhom12.example.nhom12.service.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nhom12.example.nhom12.dto.request.CreateOrderRequest;
import nhom12.example.nhom12.dto.request.CreateVoucherRequest;
import nhom12.example.nhom12.dto.request.UpdateVoucherRequest;
import nhom12.example.nhom12.dto.response.VoucherResponse;
import nhom12.example.nhom12.dto.response.VoucherValidationResponse;
import nhom12.example.nhom12.exception.BadRequestException;
import nhom12.example.nhom12.exception.DuplicateResourceException;
import nhom12.example.nhom12.exception.ResourceNotFoundException;
import nhom12.example.nhom12.model.AppliedVoucher;
import nhom12.example.nhom12.model.Order;
import nhom12.example.nhom12.model.Product;
import nhom12.example.nhom12.model.ProductVariant;
import nhom12.example.nhom12.model.Voucher;
import nhom12.example.nhom12.model.enums.VoucherDiscountType;
import nhom12.example.nhom12.model.enums.VoucherType;
import nhom12.example.nhom12.repository.ProductRepository;
import nhom12.example.nhom12.repository.VoucherRepository;
import nhom12.example.nhom12.service.VoucherService;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

  private static final double FREE_SHIPPING_THRESHOLD = 500000;
  private static final double DEFAULT_SHIPPING_FEE = 30000;

  private final VoucherRepository voucherRepository;
  private final ProductRepository productRepository;
  private final MongoTemplate mongoTemplate;

  @Override
  public VoucherResponse createVoucher(CreateVoucherRequest request) {
    String normalizedCode = normalizeCode(request.getCode());
    voucherRepository
        .findByCodeIgnoreCase(normalizedCode)
        .ifPresent(voucher -> {
          throw new DuplicateResourceException("Voucher", "code", normalizedCode);
        });

    Voucher voucher = voucherRepository.save(mapToVoucher(Voucher.builder(), request, normalizedCode).build());
    return toResponse(voucher, voucher.getMinOrderValue());
  }

  @Override
  public VoucherResponse updateVoucher(String id, UpdateVoucherRequest request) {
    Voucher existing =
        voucherRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Voucher", "id", id));
    String normalizedCode = normalizeCode(request.getCode());

    voucherRepository
        .findByCodeIgnoreCase(normalizedCode)
        .filter(voucher -> !voucher.getId().equals(id))
        .ifPresent(voucher -> {
          throw new DuplicateResourceException("Voucher", "code", normalizedCode);
        });

    Voucher updated = voucherRepository.save(mapToVoucher(Voucher.builder().id(existing.getId()).createdAt(existing.getCreatedAt()).updatedAt(existing.getUpdatedAt()).usedCount(existing.getUsedCount()), request, normalizedCode).build());
    return toResponse(updated, updated.getMinOrderValue());
  }

  @Override
  public void deleteVoucher(String id) {
    Voucher voucher =
        voucherRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Voucher", "id", id));
    voucherRepository.delete(voucher);
  }

  @Override
  public List<VoucherResponse> getAllVouchers() {
    return voucherRepository.findAllSorted().stream()
        .map(
            voucher ->
                toResponse(
                    voucher,
                    voucher.getType() == VoucherType.SHIPPING
                        ? DEFAULT_SHIPPING_FEE
                        : voucher.getMinOrderValue()))
        .toList();
  }

  @Override
  public List<VoucherResponse> getAvailableVouchers(List<CreateOrderRequest.OrderItemRequest> items) {
    double subtotal = calculateSubtotal(items);
    double shippingFee = calculateShippingFee(subtotal);

    return voucherRepository.findAllSorted().stream()
        .map(voucher -> toResponse(voucher, getBaseAmount(voucher, subtotal, shippingFee)))
        .sorted(
            (left, right) -> {
              if (left.isUsable() == right.isUsable()) {
                return left.getCode().compareToIgnoreCase(right.getCode());
              }
              return left.isUsable() ? -1 : 1;
            })
        .toList();
  }

  @Override
  public VoucherValidationResponse validateOrderVouchers(
      List<CreateOrderRequest.OrderItemRequest> items, String productVoucherCode, String shippingVoucherCode) {
    double subtotal = calculateSubtotal(items);
    double originalShippingFee = calculateShippingFee(subtotal);
    Voucher productVoucher = resolveVoucher(productVoucherCode, VoucherType.PRODUCT, subtotal);
    Voucher shippingVoucher = resolveVoucher(shippingVoucherCode, VoucherType.SHIPPING, subtotal);

    AppliedVoucher appliedProductVoucher = applyVoucher(productVoucher, subtotal);
    double discountedSubtotal = Math.max(subtotal - getDiscountAmount(appliedProductVoucher), 0);
    AppliedVoucher appliedShippingVoucher = applyVoucher(shippingVoucher, originalShippingFee);
    double shippingFee = Math.max(originalShippingFee - getDiscountAmount(appliedShippingVoucher), 0);

    return VoucherValidationResponse.builder()
        .subtotal(subtotal)
        .originalShippingFee(originalShippingFee)
        .shippingFee(shippingFee)
        .productDiscount(getDiscountAmount(appliedProductVoucher))
        .shippingDiscount(getDiscountAmount(appliedShippingVoucher))
        .totalDiscount(getDiscountAmount(appliedProductVoucher) + getDiscountAmount(appliedShippingVoucher))
        .total(discountedSubtotal + shippingFee)
        .productVoucher(appliedProductVoucher)
        .shippingVoucher(appliedShippingVoucher)
        .build();
  }

  @Override
  public void markOrderVoucherUsage(Order order) {
    adjustUsage(order.getProductVoucher(), 1);
    adjustUsage(order.getShippingVoucher(), 1);
  }

  @Override
  public void rollbackOrderVoucherUsage(Order order) {
    adjustUsage(order.getProductVoucher(), -1);
    adjustUsage(order.getShippingVoucher(), -1);
  }

  private void adjustUsage(AppliedVoucher appliedVoucher, int delta) {
    if (appliedVoucher == null || appliedVoucher.getVoucherId() == null) {
      return;
    }

    if (delta > 0) {
      reserveVoucherUsage(appliedVoucher.getVoucherId(), appliedVoucher.getCode());
      return;
    }

    releaseVoucherUsage(appliedVoucher.getVoucherId());
  }

  private double calculateSubtotal(List<CreateOrderRequest.OrderItemRequest> items) {
    return items.stream()
        .mapToDouble(this::calculateItemSubtotal)
        .sum();
  }

  private double calculateItemSubtotal(CreateOrderRequest.OrderItemRequest item) {
    Product product =
        productRepository
            .findById(item.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", item.getProductId()));
    ProductVariant variant = findVariant(product, item.getColor(), item.getStorage());
    double unitPrice = variant != null ? variant.getPrice() : product.getPrice();
    return unitPrice * item.getQuantity();
  }

  private double calculateShippingFee(double subtotal) {
    return subtotal >= FREE_SHIPPING_THRESHOLD ? 0 : DEFAULT_SHIPPING_FEE;
  }

  private Voucher resolveVoucher(String code, VoucherType expectedType, double baseAmount) {
    if (code == null || code.isBlank()) {
      return null;
    }

    Voucher voucher =
        voucherRepository
            .findByCodeIgnoreCase(normalizeCode(code))
            .orElseThrow(() -> new BadRequestException("Voucher '" + code.trim() + "' không tồn tại"));

    if (voucher.getType() != expectedType) {
      throw new BadRequestException(
          "Voucher '" + voucher.getCode() + "' không áp dụng cho loại " + expectedType.name().toLowerCase());
    }

    validateVoucher(voucher, baseAmount);
    return voucher;
  }

  private void validateVoucher(Voucher voucher, double baseAmount) {
    if (!voucher.isActive()) {
      throw new BadRequestException("Voucher '" + voucher.getCode() + "' hiện đang tắt");
    }

    LocalDateTime now = LocalDateTime.now();
    if (voucher.getStartAt() != null && now.isBefore(voucher.getStartAt())) {
      throw new BadRequestException("Voucher '" + voucher.getCode() + "' chưa đến thời gian áp dụng");
    }
    if (voucher.getEndAt() != null && now.isAfter(voucher.getEndAt())) {
      throw new BadRequestException("Voucher '" + voucher.getCode() + "' đã hết hạn");
    }
    if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
      throw new BadRequestException("Voucher '" + voucher.getCode() + "' đã hết lượt sử dụng");
    }
    if (baseAmount < voucher.getMinOrderValue()) {
      throw new BadRequestException(
          "Voucher '" + voucher.getCode() + "' yêu cầu đơn tối thiểu " + formatCurrency(voucher.getMinOrderValue()));
    }
  }

  private boolean isVoucherUsable(Voucher voucher, double baseAmount) {
    return getVoucherUnusableReason(voucher, baseAmount) == null;
  }

  private AppliedVoucher applyVoucher(Voucher voucher, double baseAmount) {
    if (voucher == null || baseAmount <= 0) {
      return null;
    }

    double discountAmount;
    if (voucher.getDiscountType() == VoucherDiscountType.PERCENTAGE) {
      discountAmount = baseAmount * voucher.getDiscountValue() / 100.0;
      if (voucher.getMaxDiscountAmount() != null) {
        discountAmount = Math.min(discountAmount, voucher.getMaxDiscountAmount());
      }
    } else {
      discountAmount = voucher.getDiscountValue();
    }

    discountAmount = Math.min(discountAmount, baseAmount);

    return AppliedVoucher.builder()
        .voucherId(voucher.getId())
        .code(voucher.getCode())
        .type(voucher.getType())
        .discountType(voucher.getDiscountType())
        .discountValue(voucher.getDiscountValue())
        .discountAmount(discountAmount)
        .build();
  }

  private double getDiscountAmount(AppliedVoucher appliedVoucher) {
    return appliedVoucher == null ? 0 : appliedVoucher.getDiscountAmount();
  }

  private String normalizeCode(String code) {
    return code == null ? "" : code.trim().toUpperCase();
  }

  private String formatCurrency(double value) {
    return String.format("%,.0fđ", value);
  }

  private double getBaseAmount(Voucher voucher, double subtotal, double shippingFee) {
    return voucher.getType() == VoucherType.SHIPPING ? shippingFee : subtotal;
  }

  private String getVoucherUnusableReason(Voucher voucher, double baseAmount) {
    try {
      validateVoucher(voucher, baseAmount);
      return null;
    } catch (BadRequestException ex) {
      return ex.getMessage();
    }
  }

  private void reserveVoucherUsage(String voucherId, String voucherCode) {
    Criteria availableCriteria =
        new Criteria()
            .orOperator(
                Criteria.where("usageLimit").is(null),
                Criteria.expr(
                    new Document("$gt", Arrays.asList("$usageLimit", "$usedCount"))));

    Query query =
        new Query(new Criteria().andOperator(Criteria.where("_id").is(voucherId), availableCriteria));
    long modified =
        mongoTemplate
            .updateFirst(query, new Update().inc("usedCount", 1), Voucher.class)
            .getModifiedCount();

    if (modified == 0) {
      throw new BadRequestException(
          "Voucher '" + voucherCode + "' vÃ¹a háº¿t lÆ°á»£t sá»­ dá»¥ng. Vui lÃ²ng chá»n mÃ£ khÃ¡c.");
    }
  }

  private void releaseVoucherUsage(String voucherId) {
    Query query =
        new Query(
            new Criteria().andOperator(
                Criteria.where("_id").is(voucherId), Criteria.where("usedCount").gt(0)));
    mongoTemplate.updateFirst(query, new Update().inc("usedCount", -1), Voucher.class);
  }

  private ProductVariant findVariant(Product product, String color, String storage) {
    String normalizedColor = normalizeOption(color);
    String normalizedStorage = normalizeOption(storage);

    if (normalizedColor.isBlank() && normalizedStorage.isBlank()) {
      return null;
    }

    if (product.getVariants() == null) {
      throw new ResourceNotFoundException(
          "Product variant",
          "productId",
          product.getId() + ":" + normalizedColor + ":" + normalizedStorage);
    }

    return product.getVariants().stream()
        .filter(
            variant ->
                normalizeOption(variant.getColor()).equals(normalizedColor)
                    && normalizeOption(variant.getStorage()).equals(normalizedStorage))
        .findFirst()
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "Product variant",
                    "productId",
                    product.getId() + ":" + normalizedColor + ":" + normalizedStorage));
  }

  private String normalizeOption(String value) {
    return value == null ? "" : value.trim();
  }

  private VoucherResponse toResponse(Voucher voucher, double baseAmount) {
    String unusableReason = getVoucherUnusableReason(voucher, baseAmount);
    boolean usable = unusableReason == null;

    return VoucherResponse.builder()
        .id(voucher.getId())
        .code(voucher.getCode())
        .description(voucher.getDescription())
        .type(voucher.getType())
        .discountType(voucher.getDiscountType())
        .discountValue(voucher.getDiscountValue())
        .maxDiscountAmount(voucher.getMaxDiscountAmount())
        .minOrderValue(voucher.getMinOrderValue())
        .usageLimit(voucher.getUsageLimit())
        .usedCount(voucher.getUsedCount())
        .startAt(voucher.getStartAt())
        .endAt(voucher.getEndAt())
        .active(voucher.isActive())
        .usable(usable)
        .estimatedDiscountAmount(
            usable ? getDiscountAmount(applyVoucher(voucher, baseAmount)) : null)
        .unusableReason(unusableReason)
        .build();
  }

  private Voucher.VoucherBuilder<?, ?> mapToVoucher(
      Voucher.VoucherBuilder<?, ?> builder, CreateVoucherRequest request, String normalizedCode) {
    validateRequest(request.getType(), request.getDiscountType(), request.getDiscountValue(), request.getMaxDiscountAmount(), request.getUsageLimit(), request.getStartAt(), request.getEndAt());
    return builder
        .code(normalizedCode)
        .description(request.getDescription())
        .type(request.getType())
        .discountType(request.getDiscountType())
        .discountValue(request.getDiscountValue())
        .maxDiscountAmount(request.getMaxDiscountAmount())
        .minOrderValue(request.getMinOrderValue())
        .usageLimit(request.getUsageLimit())
        .startAt(request.getStartAt())
        .endAt(request.getEndAt())
        .active(request.isActive());
  }

  private Voucher.VoucherBuilder<?, ?> mapToVoucher(
      Voucher.VoucherBuilder<?, ?> builder, UpdateVoucherRequest request, String normalizedCode) {
    validateRequest(request.getType(), request.getDiscountType(), request.getDiscountValue(), request.getMaxDiscountAmount(), request.getUsageLimit(), request.getStartAt(), request.getEndAt());
    return builder
        .code(normalizedCode)
        .description(request.getDescription())
        .type(request.getType())
        .discountType(request.getDiscountType())
        .discountValue(request.getDiscountValue())
        .maxDiscountAmount(request.getMaxDiscountAmount())
        .minOrderValue(request.getMinOrderValue())
        .usageLimit(request.getUsageLimit())
        .startAt(request.getStartAt())
        .endAt(request.getEndAt())
        .active(request.isActive());
  }

  private void validateRequest(
      VoucherType type,
      VoucherDiscountType discountType,
      double discountValue,
      Double maxDiscountAmount,
      Integer usageLimit,
      LocalDateTime startAt,
      LocalDateTime endAt) {
    if (discountValue <= 0) {
      throw new BadRequestException("Giá trị voucher phải lớn hơn 0");
    }
    if (discountType == VoucherDiscountType.PERCENTAGE && discountValue > 100) {
      throw new BadRequestException("Voucher phần trăm không được vượt quá 100%");
    }
    if (type == VoucherType.SHIPPING && maxDiscountAmount != null && maxDiscountAmount < 0) {
      throw new BadRequestException("Giảm tối đa không hợp lệ");
    }
    if (usageLimit != null && usageLimit < 1) {
      throw new BadRequestException("Giới hạn sử dụng phải lớn hơn 0");
    }
    if (startAt != null && endAt != null && endAt.isBefore(startAt)) {
      throw new BadRequestException("Thời gian kết thúc phải sau thời gian bắt đầu");
    }
  }
}
