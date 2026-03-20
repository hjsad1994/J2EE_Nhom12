package nhom12.example.nhom12.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nhom12.example.nhom12.model.Category;
import nhom12.example.nhom12.model.Product;
import nhom12.example.nhom12.model.ProductVariant;
import nhom12.example.nhom12.repository.CategoryRepository;
import nhom12.example.nhom12.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;

  private static ProductVariant v(String color, String storage, double price, int stock) {
    return ProductVariant.builder().color(color).storage(storage).price(price).stock(stock).build();
  }

  /** Compute product-level stock as sum of all variant stocks for consistency. */
  private static int stockOf(List<ProductVariant> variants) {
    return variants.stream().mapToInt(ProductVariant::getStock).sum();
  }

  @Override
  public void run(String... args) {
    if (productRepository.count() > 0) {
      return;
    }

    // ── Categories ──────────────────────────────────────────────────────────
    categoryRepository.deleteAll();
    List<Category> categories =
        List.of(
            Category.builder()
                .name("Flagship")
                .slug("flagship")
                .description("Điện thoại cao cấp hàng đầu")
                .icon("crown")
                .build(),
            Category.builder()
                .name("Tầm trung")
                .slug("tam-trung")
                .description("Điện thoại tầm trung giá tốt")
                .icon("smartphone")
                .build(),
            Category.builder()
                .name("Gaming")
                .slug("gaming")
                .description("Điện thoại chuyên game hiệu năng cao")
                .icon("gamepad-2")
                .build(),
            Category.builder()
                .name("Camera")
                .slug("camera")
                .description("Điện thoại chuyên chụp ảnh")
                .icon("camera")
                .build());

    List<Category> saved = categoryRepository.saveAll(categories);
    String flagshipId = saved.get(0).getId();
    String midRangeId = saved.get(1).getId();
    String gamingId = saved.get(2).getId();
    String cameraId = saved.get(3).getId();

    // ── Products ─────────────────────────────────────────────────────────────
    // NOTE: product.stock = stockOf(variants) to keep product-level stock
    //       consistent with variant-level stock for order processing.

    List<Product> products =
        List.of(

            // ═══════════════════════════════════════════════
            // FLAGSHIP
            // ═══════════════════════════════════════════════

            // 1. iPhone 16 Pro Max
            Product.builder()
                .name("iPhone 16 Pro Max")
                .brand("Apple")
                .categoryId(flagshipId)
                .price(34990000)
                .originalPrice(37990000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/i/p/iphone-16-pro-max_1.png")
                .rating(4.9)
                .badge("Hot")
                .specs(
                    "Chip A18 Pro 3nm · Camera 48MP Fusion + 12MP Ultrawide + 12MP 5x Telemacro"
                        + " · Màn 6.9\" Super Retina XDR ProMotion 120Hz · Pin 4685mAh USB-C ·"
                        + " IP68 · 5G · Face ID")
                .variants(
                    List.of(
                        v("Titan Đen", "256GB", 34990000, 30),
                        v("Titan Đen", "512GB", 39990000, 20),
                        v("Titan Đen", "1TB", 44990000, 10),
                        v("Titan Trắng", "256GB", 34990000, 25),
                        v("Titan Trắng", "512GB", 39990000, 15),
                        v("Titan Trắng", "1TB", 44990000, 8),
                        v("Titan Sa Mạc", "256GB", 34990000, 20),
                        v("Titan Sa Mạc", "512GB", 39990000, 12)))
                .build(),

            // 2. iPhone 16 Pro
            Product.builder()
                .name("iPhone 16 Pro")
                .brand("Apple")
                .categoryId(flagshipId)
                .price(28990000)
                .originalPrice(30990000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/i/p/iphone-16-pro_1.png")
                .rating(4.8)
                .badge("New")
                .specs(
                    "Chip A18 Pro 3nm · Camera 48MP Fusion + 12MP Ultrawide + 12MP 5x Telemacro"
                        + " · Màn 6.3\" Super Retina XDR ProMotion 120Hz · Pin 3582mAh · IP68 ·"
                        + " 5G · Dynamic Island")
                .variants(
                    List.of(
                        v("Titan Đen", "128GB", 28990000, 35),
                        v("Titan Đen", "256GB", 31990000, 20),
                        v("Titan Đen", "512GB", 36990000, 10),
                        v("Titan Trắng", "128GB", 28990000, 30),
                        v("Titan Trắng", "256GB", 31990000, 18),
                        v("Titan Sa Mạc", "128GB", 28990000, 25),
                        v("Titan Sa Mạc", "256GB", 31990000, 15)))
                .build(),

            // 3. iPhone 16
            Product.builder()
                .name("iPhone 16")
                .brand("Apple")
                .categoryId(flagshipId)
                .price(22990000)
                .originalPrice(24990000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/i/p/iphone-16_1.png")
                .rating(4.7)
                .specs(
                    "Chip A18 3nm · Camera Fusion 48MP + 12MP Ultrawide · Màn 6.1\" Super Retina"
                        + " XDR 60Hz · Dynamic Island · Pin 3561mAh · IP68 · 5G")
                .variants(
                    List.of(
                        v("Đen", "128GB", 22990000, 40),
                        v("Đen", "256GB", 25990000, 25),
                        v("Trắng", "128GB", 22990000, 35),
                        v("Trắng", "256GB", 25990000, 20),
                        v("Hồng", "128GB", 22990000, 30),
                        v("Hồng", "256GB", 25990000, 15),
                        v("Xanh", "128GB", 22990000, 25),
                        v("Xanh", "256GB", 25990000, 10)))
                .build(),

            // 4. iPhone 15
            Product.builder()
                .name("iPhone 15")
                .brand("Apple")
                .categoryId(flagshipId)
                .price(18990000)
                .originalPrice(22990000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/i/p/iphone-15-den.png")
                .rating(4.6)
                .badge("Sale")
                .specs(
                    "Chip A16 Bionic · Camera 48MP Fusion + 12MP Ultrawide · Màn 6.1\" Super"
                        + " Retina XDR · Dynamic Island · USB-C · Pin 3349mAh · IP68 · 5G")
                .variants(
                    List.of(
                        v("Đen", "128GB", 18990000, 45),
                        v("Đen", "256GB", 21990000, 25),
                        v("Hồng", "128GB", 18990000, 40),
                        v("Hồng", "256GB", 21990000, 20),
                        v("Vàng", "128GB", 18990000, 35),
                        v("Vàng", "256GB", 21990000, 18),
                        v("Xanh", "128GB", 18990000, 30),
                        v("Xanh", "256GB", 21990000, 15)))
                .build(),

            // 5. Samsung Galaxy S25 Ultra
            Product.builder()
                .name("Samsung Galaxy S25 Ultra")
                .brand("Samsung")
                .categoryId(flagshipId)
                .price(31990000)
                .originalPrice(34990000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/s/a/samsung-galaxy-s25-ultra.png")
                .rating(4.8)
                .badge("Hot")
                .specs(
                    "Snapdragon 8 Elite · Camera 200MP + 50MP Ultrawide + 10MP 3x + 50MP 5x ·"
                        + " Màn 6.9\" Dynamic AMOLED 2X 120Hz · S Pen tích hợp · Pin 5000mAh ·"
                        + " IP68 · 5G · Wi-Fi 7")
                .variants(
                    List.of(
                        v("Titan Bạc", "256GB", 31990000, 25),
                        v("Titan Bạc", "512GB", 36990000, 15),
                        v("Titan Bạc", "1TB", 41990000, 8),
                        v("Titan Đen", "256GB", 31990000, 20),
                        v("Titan Đen", "512GB", 36990000, 12),
                        v("Titan Đen", "1TB", 41990000, 5),
                        v("Titan Xanh", "256GB", 31990000, 18),
                        v("Titan Xanh", "512GB", 36990000, 10)))
                .build(),

            // 6. Samsung Galaxy S25+
            Product.builder()
                .name("Samsung Galaxy S25+")
                .brand("Samsung")
                .categoryId(flagshipId)
                .price(24990000)
                .originalPrice(26990000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/s/a/samsung-galaxy-s25-plus.png")
                .rating(4.7)
                .badge("New")
                .specs(
                    "Snapdragon 8 Elite · Camera 50MP + 12MP Ultrawide + 10MP 3x · Màn 6.7\""
                        + " Dynamic AMOLED 2X 120Hz · Pin 4900mAh · IP68 · 5G · Wi-Fi 7 · Galaxy"
                        + " AI")
                .variants(
                    List.of(
                        v("Bạc", "256GB", 24990000, 30),
                        v("Bạc", "512GB", 29990000, 15),
                        v("Đen", "256GB", 24990000, 25),
                        v("Đen", "512GB", 29990000, 12),
                        v("Xanh Navy", "256GB", 24990000, 20),
                        v("Xanh Navy", "512GB", 29990000, 10)))
                .build(),

            // 7. Samsung Galaxy S25
            Product.builder()
                .name("Samsung Galaxy S25")
                .brand("Samsung")
                .categoryId(flagshipId)
                .price(20990000)
                .originalPrice(22990000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/s/a/samsung-galaxy-s25.png")
                .rating(4.6)
                .specs(
                    "Snapdragon 8 Elite · Camera 50MP + 12MP Ultrawide + 10MP 3x · Màn 6.2\""
                        + " Dynamic AMOLED 2X 120Hz · Pin 4000mAh · IP68 · 5G · Galaxy AI")
                .variants(
                    List.of(
                        v("Bạc", "128GB", 20990000, 35),
                        v("Bạc", "256GB", 23990000, 20),
                        v("Đen", "128GB", 20990000, 30),
                        v("Đen", "256GB", 23990000, 18),
                        v("Xanh", "128GB", 20990000, 25),
                        v("Xanh", "256GB", 23990000, 15)))
                .build(),

            // 8. Samsung Galaxy Z Fold 6
            Product.builder()
                .name("Samsung Galaxy Z Fold 6")
                .brand("Samsung")
                .categoryId(flagshipId)
                .price(44990000)
                .originalPrice(48990000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/s/a/samsung-galaxy-z-fold-6.png")
                .rating(4.7)
                .badge("Premium")
                .specs(
                    "Snapdragon 8 Gen 3 · Màn trong 7.6\" Dynamic AMOLED 2X 120Hz · Màn ngoài"
                        + " 6.3\" · Camera 50MP + 10MP + 10MP · IPX8 · S Pen tương thích · 5G")
                .variants(
                    List.of(
                        v("Xanh Navy", "256GB", 44990000, 15),
                        v("Xanh Navy", "512GB", 49990000, 8),
                        v("Bạc", "256GB", 44990000, 12),
                        v("Bạc", "512GB", 49990000, 6),
                        v("Hồng", "256GB", 44990000, 10),
                        v("Hồng", "512GB", 49990000, 5)))
                .build(),

            // 9. Samsung Galaxy Z Flip 6
            Product.builder()
                .name("Samsung Galaxy Z Flip 6")
                .brand("Samsung")
                .categoryId(flagshipId)
                .price(24990000)
                .originalPrice(26990000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/s/a/samsung-galaxy-z-flip-6.png")
                .rating(4.5)
                .specs(
                    "Snapdragon 8 Gen 3 · Màn gập 6.7\" Dynamic AMOLED 120Hz · Màn phụ 3.4\" ·"
                        + " Camera 50MP + 12MP · Pin 4000mAh · IPX8 · 5G · Flex Mode AI")
                .variants(
                    List.of(
                        v("Vàng", "256GB", 24990000, 20),
                        v("Vàng", "512GB", 28990000, 10),
                        v("Bạc", "256GB", 24990000, 18),
                        v("Bạc", "512GB", 28990000, 8),
                        v("Đen", "256GB", 24990000, 15),
                        v("Đen", "512GB", 28990000, 7),
                        v("Xanh", "256GB", 24990000, 12),
                        v("Hồng", "256GB", 24990000, 10)))
                .build(),

            // 10. OnePlus 13
            Product.builder()
                .name("OnePlus 13")
                .brand("OnePlus")
                .categoryId(flagshipId)
                .price(20990000)
                .originalPrice(22990000.0)
                .image("https://fdn2.gsmarena.com/vv/pics/oneplus/oneplus-13-1.jpg")
                .rating(4.6)
                .specs(
                    "Snapdragon 8 Elite · Hasselblad Camera 50MP + 50MP Ultrawide + 50MP 3x ·"
                        + " Màn 6.82\" LTPO AMOLED 120Hz · Pin 6000mAh · Sạc nhanh 100W · Sạc"
                        + " không dây 50W · IP65")
                .variants(
                    List.of(
                        v("Đen Huyền Bí", "256GB", 20990000, 25),
                        v("Đen Huyền Bí", "512GB", 24990000, 15),
                        v("Trắng Tuyết", "256GB", 20990000, 20),
                        v("Trắng Tuyết", "512GB", 24990000, 12)))
                .build(),

            // ═══════════════════════════════════════════════
            // TẦM TRUNG
            // ═══════════════════════════════════════════════

            // 11. Samsung Galaxy A56 5G
            Product.builder()
                .name("Samsung Galaxy A56 5G")
                .brand("Samsung")
                .categoryId(midRangeId)
                .price(9990000)
                .originalPrice(11990000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/s/a/samsung-galaxy-a56.png")
                .rating(4.4)
                .badge("Best Seller")
                .specs(
                    "Exynos 1580 · Camera OIS 50MP + 12MP Ultrawide + 5MP Macro · Màn 6.7\""
                        + " Super AMOLED 120Hz · Pin 5000mAh · IP67 · 5G · RAM 8GB")
                .variants(
                    List.of(
                        v("Đen", "128GB", 9990000, 50),
                        v("Đen", "256GB", 11990000, 30),
                        v("Xanh", "128GB", 9990000, 45),
                        v("Xanh", "256GB", 11990000, 25),
                        v("Tím", "128GB", 9990000, 40),
                        v("Tím", "256GB", 11990000, 20)))
                .build(),

            // 12. Samsung Galaxy A35 5G
            Product.builder()
                .name("Samsung Galaxy A35 5G")
                .brand("Samsung")
                .categoryId(midRangeId)
                .price(7490000)
                .originalPrice(8990000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/s/a/samsung-galaxy-a35-5g.png")
                .rating(4.3)
                .badge("Sale")
                .specs(
                    "Exynos 1380 · Camera OIS 50MP + 8MP Ultrawide + 5MP Macro · Màn 6.6\""
                        + " Super AMOLED 120Hz · Pin 5000mAh · IP67 · 5G · RAM 8GB")
                .variants(
                    List.of(
                        v("Đen", "128GB", 7490000, 60),
                        v("Đen", "256GB", 8990000, 35),
                        v("Xanh Dương", "128GB", 7490000, 55),
                        v("Xanh Dương", "256GB", 8990000, 30),
                        v("Tím Nhạt", "128GB", 7490000, 50),
                        v("Tím Nhạt", "256GB", 8990000, 25)))
                .build(),

            // 13. iPhone 14
            Product.builder()
                .name("iPhone 14")
                .brand("Apple")
                .categoryId(midRangeId)
                .price(16990000)
                .originalPrice(20990000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/i/p/iphone-14-den.png")
                .rating(4.5)
                .badge("Sale")
                .specs(
                    "Chip A15 Bionic · Camera 12MP Fusion + 12MP Ultrawide · Màn 6.1\" Super"
                        + " Retina XDR · Pin 3279mAh · IP68 · 5G · Crash Detection")
                .variants(
                    List.of(
                        v("Đen", "128GB", 16990000, 35),
                        v("Đen", "256GB", 19990000, 20),
                        v("Tím", "128GB", 16990000, 30),
                        v("Tím", "256GB", 19990000, 15),
                        v("Đỏ", "128GB", 16990000, 25),
                        v("Đỏ", "256GB", 19990000, 12),
                        v("Vàng", "128GB", 16990000, 20),
                        v("Xanh", "128GB", 16990000, 18)))
                .build(),

            // 14. Xiaomi Redmi Note 14 Pro+
            Product.builder()
                .name("Xiaomi Redmi Note 14 Pro+")
                .brand("Xiaomi")
                .categoryId(midRangeId)
                .price(8490000)
                .originalPrice(9990000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/r/e/redmi-note-14-pro-plus.png")
                .rating(4.4)
                .badge("Value")
                .specs(
                    "Dimensity 1400 Ultra · Camera 200MP OIS + 8MP Ultrawide + 2MP Macro · Màn"
                        + " 6.67\" OLED 120Hz 1.5K · Pin 6200mAh · Sạc 90W · IP68 · 5G · RAM"
                        + " 8/12GB")
                .variants(
                    List.of(
                        v("Đen", "256GB", 8490000, 40),
                        v("Đen", "512GB", 9990000, 25),
                        v("Trắng", "256GB", 8490000, 35),
                        v("Trắng", "512GB", 9990000, 20),
                        v("Tím", "256GB", 8490000, 30),
                        v("Tím", "512GB", 9990000, 15)))
                .build(),

            // 15. Xiaomi Redmi Note 14
            Product.builder()
                .name("Xiaomi Redmi Note 14")
                .brand("Xiaomi")
                .categoryId(midRangeId)
                .price(5990000)
                .originalPrice(6990000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/r/e/redmi-note-14.png")
                .rating(4.2)
                .specs(
                    "Snapdragon 7s Gen 3 · Camera 108MP + 8MP Ultrawide + 2MP Macro · Màn 6.67\""
                        + " AMOLED 120Hz · Pin 5500mAh · Sạc 45W · 5G · RAM 8GB")
                .variants(
                    List.of(
                        v("Đen", "128GB", 5990000, 50),
                        v("Đen", "256GB", 6990000, 30),
                        v("Xanh", "128GB", 5990000, 45),
                        v("Xanh", "256GB", 6990000, 25)))
                .build(),

            // 16. OPPO Reno 12 Pro
            Product.builder()
                .name("OPPO Reno 12 Pro")
                .brand("OPPO")
                .categoryId(midRangeId)
                .price(10990000)
                .originalPrice(12990000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/o/p/oppo-reno-12-pro.png")
                .rating(4.3)
                .badge("New")
                .specs(
                    "Dimensity 7300 Energy · Camera 50MP Sony IMX890 OIS + 8MP Ultrawide + 50MP"
                        + " 2x · Màn 6.7\" AMOLED 120Hz · Pin 5000mAh · Sạc SUPERVOOC 80W · 5G"
                        + " · AI Portrait")
                .variants(
                    List.of(
                        v("Xanh Lá", "256GB", 10990000, 35),
                        v("Đen", "256GB", 10990000, 30),
                        v("Hồng", "256GB", 10990000, 25),
                        v("Nâu", "256GB", 10990000, 20)))
                .build(),

            // 17. Vivo V40
            Product.builder()
                .name("Vivo V40")
                .brand("Vivo")
                .categoryId(midRangeId)
                .price(10490000)
                .originalPrice(11990000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/v/i/vivo-v40.png")
                .rating(4.3)
                .specs(
                    "Snapdragon 7 Gen 3 · ZEISS Camera 50MP OIS + 50MP 2x + 8MP Ultrawide · Màn"
                        + " 6.78\" AMOLED 120Hz · Pin 5500mAh · Sạc 80W · 5G · IP64")
                .variants(
                    List.of(
                        v("Đen", "256GB", 10490000, 30),
                        v("Hồng", "256GB", 10490000, 25),
                        v("Xanh", "256GB", 10490000, 22),
                        v("Vàng Đồng", "256GB", 10490000, 18)))
                .build(),

            // 18. Realme 13 Pro+
            Product.builder()
                .name("Realme 13 Pro+")
                .brand("Realme")
                .categoryId(midRangeId)
                .price(8990000)
                .originalPrice(9990000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/r/e/realme-13-pro-plus.png")
                .rating(4.2)
                .specs(
                    "Snapdragon 7s Gen 3 · Sony IMX890 Camera 50MP OIS + 8MP Ultrawide + 50MP 3x"
                        + " · Màn 6.7\" OLED 120Hz · Pin 5200mAh · Sạc SUPERVOOC 80W · 5G")
                .variants(
                    List.of(
                        v("Đen Ngọc Trai", "256GB", 8990000, 40),
                        v("Xanh", "256GB", 8990000, 35),
                        v("Vàng", "256GB", 8990000, 25),
                        v("Đen Ngọc Trai", "512GB", 10490000, 20)))
                .build(),

            // 19. OPPO A3 Pro
            Product.builder()
                .name("OPPO A3 Pro")
                .brand("OPPO")
                .categoryId(midRangeId)
                .price(5490000)
                .originalPrice(6490000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/o/p/oppo-a3-pro.png")
                .rating(4.1)
                .specs(
                    "Dimensity 6300 · Camera 64MP + 2MP Macro · Màn 6.7\" LCD 90Hz · Pin"
                        + " 5100mAh · Sạc 45W · IP54 · RAM 8GB · Chống nước cơ bản")
                .variants(
                    List.of(
                        v("Xanh Dương", "128GB", 5490000, 55),
                        v("Đen", "128GB", 5490000, 50),
                        v("Hồng", "128GB", 5490000, 45),
                        v("Xanh Dương", "256GB", 6490000, 30)))
                .build(),

            // ═══════════════════════════════════════════════
            // GAMING
            // ═══════════════════════════════════════════════

            // 20. ASUS ROG Phone 8 Pro
            Product.builder()
                .name("ASUS ROG Phone 8 Pro")
                .brand("ASUS")
                .categoryId(gamingId)
                .price(27990000)
                .originalPrice(30990000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/a/s/asus-rog-phone-8-pro.png")
                .rating(4.7)
                .badge("Gaming")
                .specs(
                    "Snapdragon 8 Gen 3 · Màn 6.78\" AMOLED 165Hz 2160Hz Touch Sampling · Camera"
                        + " 50MP + 13MP + 32MP · AeroActive Cooler X · Pin 5500mAh · Sạc 65W ·"
                        + " IP68 · Dual Speaker · Game Genie · 5G")
                .variants(
                    List.of(
                        v("Đen Phantom", "256GB", 27990000, 15),
                        v("Đen Phantom", "512GB", 32990000, 8),
                        v("Trắng Storm", "256GB", 27990000, 12),
                        v("Trắng Storm", "512GB", 32990000, 6)))
                .build(),

            // 21. Nubia Red Magic 9 Pro
            Product.builder()
                .name("Nubia Red Magic 9 Pro")
                .brand("Nubia")
                .categoryId(gamingId)
                .price(17990000)
                .originalPrice(19990000.0)
                .image("https://fdn2.gsmarena.com/vv/pics/nubia/nubia-red-magic-9-pro-1.jpg")
                .rating(4.5)
                .badge("Gaming")
                .specs(
                    "Snapdragon 8 Gen 3 · Màn 6.8\" OLED 165Hz · Quạt tản nhiệt tích hợp ·"
                        + " Camera 50MP + 8MP + 2MP · Pin 6500mAh · Sạc 80W · 5G · RGB Gaming"
                        + " Light · Shoulder Trigger Buttons")
                .variants(
                    List.of(
                        v("Đen", "256GB", 17990000, 20),
                        v("Đen", "512GB", 20990000, 12),
                        v("Bạc", "256GB", 17990000, 18),
                        v("Bạc", "512GB", 20990000, 10)))
                .build(),

            // 22. Realme GT7 Pro
            Product.builder()
                .name("Realme GT7 Pro")
                .brand("Realme")
                .categoryId(gamingId)
                .price(14990000)
                .originalPrice(16990000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/r/e/realme-gt-7-pro.png")
                .rating(4.4)
                .badge("Value")
                .specs(
                    "Snapdragon 8 Elite · Màn 6.78\" LTPO OLED 120Hz · Camera 50MP Sony LYT-808"
                        + " OIS + 8MP + 2MP · Pin 6500mAh · Sạc SUPERVOOC 120W · IP69 · Vapor"
                        + " Chamber 6500mm² · 5G")
                .variants(
                    List.of(
                        v("Đen Vũ Trụ", "256GB", 14990000, 30),
                        v("Đen Vũ Trụ", "512GB", 17990000, 20),
                        v("Xám Titan", "256GB", 14990000, 25),
                        v("Xám Titan", "512GB", 17990000, 15)))
                .build(),

            // 23. Xiaomi Poco X6 Pro
            Product.builder()
                .name("Xiaomi Poco X6 Pro")
                .brand("Xiaomi")
                .categoryId(gamingId)
                .price(7490000)
                .originalPrice(8990000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/p/o/poco-x6-pro.png")
                .rating(4.3)
                .badge("Value")
                .specs(
                    "Dimensity 8300 Ultra · Màn 6.67\" OLED 144Hz · Camera 64MP + 8MP + 2MP ·"
                        + " Pin 5000mAh · Sạc 67W · 5G · Liquid Cool Technology · RAM 12GB")
                .variants(
                    List.of(
                        v("Đen", "256GB", 7490000, 40),
                        v("Vàng", "256GB", 7490000, 35),
                        v("Xám", "256GB", 7490000, 30),
                        v("Đen", "512GB", 8990000, 20)))
                .build(),

            // ═══════════════════════════════════════════════
            // CAMERA
            // ═══════════════════════════════════════════════

            // 24. Google Pixel 9 Pro
            Product.builder()
                .name("Google Pixel 9 Pro")
                .brand("Google")
                .categoryId(cameraId)
                .price(24990000)
                .originalPrice(26990000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/g/o/google-pixel-9-pro.png")
                .rating(4.7)
                .specs(
                    "Google Tensor G4 · Camera 50MP OIS + 48MP Ultrawide + 48MP 5x Telephoto ·"
                        + " Màn 6.3\" OLED LTPO 120Hz · Pin 4700mAh · 7 năm cập nhật Android ·"
                        + " Google AI · IP68 · 5G")
                .variants(
                    List.of(
                        v("Đen Bóng", "128GB", 24990000, 20),
                        v("Đen Bóng", "256GB", 27990000, 15),
                        v("Đen Bóng", "512GB", 32990000, 8),
                        v("Trắng Sứ", "128GB", 24990000, 18),
                        v("Trắng Sứ", "256GB", 27990000, 12),
                        v("Xanh Mã Não", "128GB", 24990000, 15),
                        v("Xanh Mã Não", "256GB", 27990000, 10),
                        v("Hồng", "128GB", 24990000, 12)))
                .build(),

            // 25. Google Pixel 9
            Product.builder()
                .name("Google Pixel 9")
                .brand("Google")
                .categoryId(cameraId)
                .price(18990000)
                .originalPrice(20990000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/g/o/google-pixel-9.png")
                .rating(4.5)
                .specs(
                    "Google Tensor G4 · Camera 50MP OIS + 10.5MP Ultrawide · Màn 6.3\" OLED"
                        + " 120Hz · Pin 4700mAh · Gemini AI · Magic Eraser · Best Take · IP68 ·"
                        + " 5G · 7 năm cập nhật")
                .variants(
                    List.of(
                        v("Đen", "128GB", 18990000, 30),
                        v("Đen", "256GB", 21990000, 18),
                        v("Trắng", "128GB", 18990000, 25),
                        v("Trắng", "256GB", 21990000, 15),
                        v("Hồng Sen", "128GB", 18990000, 20),
                        v("Hồng Sen", "256GB", 21990000, 12)))
                .build(),

            // 26. Xiaomi 15 Ultra
            Product.builder()
                .name("Xiaomi 15 Ultra")
                .brand("Xiaomi")
                .categoryId(cameraId)
                .price(25990000)
                .originalPrice(27990000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/x/i/xiaomi-15-ultra.png")
                .rating(4.8)
                .badge("New")
                .specs(
                    "Snapdragon 8 Elite · Leica Camera 50MP LYT-900 1\" OIS + 200MP Periscope"
                        + " Telephoto + 50MP Ultrawide + 50MP 3x · Màn 6.73\" OLED LTPO 120Hz ·"
                        + " Pin 6000mAh · Sạc 90W · IP68 · 5G")
                .variants(
                    List.of(
                        v("Đen", "256GB", 25990000, 25),
                        v("Đen", "512GB", 30990000, 15),
                        v("Trắng", "256GB", 25990000, 20),
                        v("Trắng", "512GB", 30990000, 12)))
                .build(),

            // 27. Xiaomi 15
            Product.builder()
                .name("Xiaomi 15")
                .brand("Xiaomi")
                .categoryId(flagshipId)
                .price(16990000)
                .originalPrice(18990000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/x/i/xiaomi-15.png")
                .rating(4.5)
                .specs(
                    "Snapdragon 8 Elite · Leica Camera 50MP OIS + 50MP Ultrawide + 50MP 5x ·"
                        + " Màn 6.36\" OLED LTPO 120Hz · Pin 5240mAh · Sạc 90W · Sạc không dây"
                        + " 50W · IP68 · 5G")
                .variants(
                    List.of(
                        v("Đen", "256GB", 16990000, 30),
                        v("Đen", "512GB", 20990000, 18),
                        v("Trắng", "256GB", 16990000, 25),
                        v("Trắng", "512GB", 20990000, 15),
                        v("Xanh", "256GB", 16990000, 20),
                        v("Xanh", "512GB", 20990000, 10)))
                .build(),

            // 28. OPPO Find X8 Pro
            Product.builder()
                .name("OPPO Find X8 Pro")
                .brand("OPPO")
                .categoryId(cameraId)
                .price(23990000)
                .originalPrice(25990000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/o/p/oppo-find-x8-pro.png")
                .rating(4.5)
                .specs(
                    "Dimensity 9400 · Hasselblad Camera 50MP OIS + 50MP Ultrawide + 50MP 3x +"
                        + " 50MP 6x · Màn 6.78\" OLED LTPO 120Hz · Pin 5910mAh · Sạc"
                        + " SUPERVOOC 80W · IP69 · 5G")
                .variants(
                    List.of(
                        v("Đen", "256GB", 23990000, 20),
                        v("Đen", "512GB", 27990000, 12),
                        v("Trắng", "256GB", 23990000, 18),
                        v("Trắng", "512GB", 27990000, 10)))
                .build(),

            // 29. Vivo X200 Pro
            Product.builder()
                .name("Vivo X200 Pro")
                .brand("Vivo")
                .categoryId(cameraId)
                .price(23990000)
                .originalPrice(25990000.0)
                .image(
                    "https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/v/i/vivo-x200-pro.png")
                .rating(4.5)
                .specs(
                    "Dimensity 9400 · ZEISS Camera 50MP OIS + 50MP Ultrawide + 200MP 4.3x"
                        + " Periscope · Màn 6.78\" AMOLED LTPO 120Hz · Pin 6000mAh · Sạc 90W ·"
                        + " IP68 · 5G")
                .variants(
                    List.of(
                        v("Đen", "256GB", 23990000, 20),
                        v("Đen", "512GB", 27990000, 12),
                        v("Xanh", "256GB", 23990000, 18),
                        v("Xanh", "512GB", 27990000, 10)))
                .build(),

            // 30. Sony Xperia 1 VI
            Product.builder()
                .name("Sony Xperia 1 VI")
                .brand("Sony")
                .categoryId(cameraId)
                .price(29990000)
                .originalPrice(32990000.0)
                .image("https://fdn2.gsmarena.com/vv/pics/sony/sony-xperia-1-vi-1.jpg")
                .rating(4.4)
                .specs(
                    "Snapdragon 8 Gen 3 · Camera 52MP OIS + 12MP Ultrawide + 12MP 3.5-7.1x"
                        + " Optical Zoom Telemacro · Màn 6.5\" OLED 120Hz · Pin 5000mAh · IP65 ·"
                        + " 3.5mm Jack · Stereo Speakers · 5G · BRAVIA Camera App")
                .variants(
                    List.of(
                        v("Đen Khaki", "256GB", 29990000, 15),
                        v("Đen Khaki", "512GB", 34990000, 8),
                        v("Bạch Kim", "256GB", 29990000, 12),
                        v("Xanh", "256GB", 29990000, 10)))
                .build());

    // Set product-level stock = sum of variant stocks before saving
    List<Product> productsWithStock =
        products.stream()
            .map(
                p -> {
                  if (p.getVariants() != null && !p.getVariants().isEmpty()) {
                    p.setStock(stockOf(p.getVariants()));
                  }
                  return p;
                })
            .toList();

    productRepository.saveAll(productsWithStock);
    System.out.println(
        ">>> Seeded "
            + saved.size()
            + " categories and "
            + productsWithStock.size()
            + " products.");
  }
}
