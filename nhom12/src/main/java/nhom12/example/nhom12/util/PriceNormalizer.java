package nhom12.example.nhom12.util;

import java.util.List;
import nhom12.example.nhom12.model.Product;
import nhom12.example.nhom12.model.ProductVariant;

public final class PriceNormalizer {

  private static final double LEGACY_MILLION_THRESHOLD = 1_000d;
  private static final double MILLION_TO_VND = 1_000_000d;

  private PriceNormalizer() {}

  /**
   * Legacy data may store phone prices as "triệu" shorthand (e.g. 4.99 for 4,990,000₫). Convert
   * them back to integer VND before any calculation or response mapping.
   */
  public static double normalize(double rawPrice) {
    if (Double.isNaN(rawPrice) || Double.isInfinite(rawPrice) || rawPrice <= 0) {
      return 0;
    }

    double normalized =
        rawPrice < LEGACY_MILLION_THRESHOLD ? rawPrice * MILLION_TO_VND : rawPrice;
    return Math.round(normalized);
  }

  public static Double normalizeNullable(Double rawPrice) {
    return rawPrice == null ? null : normalize(rawPrice);
  }

  public static void normalizeProduct(Product product) {
    if (product == null) {
      return;
    }

    product.setPrice(normalize(product.getPrice()));
    product.setOriginalPrice(normalizeNullable(product.getOriginalPrice()));

    List<ProductVariant> variants = product.getVariants();
    if (variants != null) {
      variants.forEach(PriceNormalizer::normalizeVariant);
    }
  }

  public static void normalizeVariant(ProductVariant variant) {
    if (variant == null) {
      return;
    }

    variant.setPrice(normalize(variant.getPrice()));
  }
}
