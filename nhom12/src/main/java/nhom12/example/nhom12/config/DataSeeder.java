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
    List<Product> products =
        List.of(

            // 1. iPhone 16 Pro Max
            Product.builder()
                .name("iPhone 16 Pro Max")
                .brand("Apple")
                .categoryId(flagshipId)
                .price(34990000)
                .originalPrice(37990000.0)
                .image("https://fdn2.gsmarena.com/vv/pics/apple/apple-iphone-16-pro-max-1.jpg")
                .rating(4.9)
                .badge("Hot")
                .specs("Chip A18 Pro · Camera 48MP · Pin 4685mAh · IP68")
                .stock(0)
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
                .image("https://fdn2.gsmarena.com/vv/pics/apple/apple-iphone-16-pro-1.jpg")
                .rating(4.8)
                .badge("New")
                .specs("Chip A18 Pro · Camera 48MP · Màn 6.3\" ProMotion · IP68")
                .stock(0)
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
                .image("https://fdn2.gsmarena.com/vv/pics/apple/apple-iphone-16-1.jpg")
                .rating(4.7)
                .specs("Chip A18 · Camera Fusion 48MP · Dynamic Island · IP68")
                .stock(0)
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

            // 4. Samsung Galaxy S25 Ultra
            Product.builder()
                .name("Samsung Galaxy S25 Ultra")
                .brand("Samsung")
                .categoryId(flagshipId)
                .price(31990000)
                .originalPrice(34990000.0)
                .image("https://fdn2.gsmarena.com/vv/pics/samsung/samsung-galaxy-s25-ultra-1.jpg")
                .rating(4.8)
                .badge("Hot")
                .specs("Snapdragon 8 Elite · Camera 200MP · S Pen · IP68")
                .stock(0)
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

            // 5. Samsung Galaxy S25+
            Product.builder()
                .name("Samsung Galaxy S25+")
                .brand("Samsung")
                .categoryId(flagshipId)
                .price(24990000)
                .originalPrice(26990000.0)
                .image("https://fdn2.gsmarena.com/vv/pics/samsung/samsung-galaxy-s25plus-1.jpg")
                .rating(4.7)
                .badge("New")
                .specs("Snapdragon 8 Elite · Camera 50MP · Màn 6.7\" Dynamic AMOLED · IP68")
                .stock(0)
                .variants(
                    List.of(
                        v("Bạc", "256GB", 24990000, 30),
                        v("Bạc", "512GB", 29990000, 15),
                        v("Đen", "256GB", 24990000, 25),
                        v("Đen", "512GB", 29990000, 12),
                        v("Xanh Navy", "256GB", 24990000, 20),
                        v("Xanh Navy", "512GB", 29990000, 10)))
                .build(),

            // 6. Samsung Galaxy A56 5G
            Product.builder()
                .name("Samsung Galaxy A56 5G")
                .brand("Samsung")
                .categoryId(midRangeId)
                .price(9990000)
                .originalPrice(11990000.0)
                .image("https://fdn2.gsmarena.com/vv/pics/samsung/samsung-galaxy-a55-5g-1.jpg")
                .rating(4.4)
                .badge("Best Seller")
                .specs("Exynos 1580 · Camera OIS 50MP · Pin 5000mAh · IP67")
                .stock(0)
                .variants(
                    List.of(
                        v("Đen", "128GB", 9990000, 50),
                        v("Đen", "256GB", 11990000, 30),
                        v("Xanh", "128GB", 9990000, 45),
                        v("Xanh", "256GB", 11990000, 25),
                        v("Tím", "128GB", 9990000, 40),
                        v("Tím", "256GB", 11990000, 20)))
                .build(),

            // 7. Google Pixel 9 Pro
            Product.builder()
                .name("Google Pixel 9 Pro")
                .brand("Google")
                .categoryId(cameraId)
                .price(24990000)
                .originalPrice(26990000.0)
                .image("https://fdn2.gsmarena.com/vv/pics/google/google-pixel-9-pro-1.jpg")
                .rating(4.7)
                .specs("Tensor G4 · Camera AI 50MP · 7 năm cập nhật · IP68")
                .stock(0)
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

            // 8. Google Pixel 9
            Product.builder()
                .name("Google Pixel 9")
                .brand("Google")
                .categoryId(cameraId)
                .price(18990000)
                .originalPrice(20990000.0)
                .image("https://fdn2.gsmarena.com/vv/pics/google/google-pixel-9-1.jpg")
                .rating(4.5)
                .specs("Tensor G4 · Camera 50MP · Gemini AI · IP68")
                .stock(0)
                .variants(
                    List.of(
                        v("Đen", "128GB", 18990000, 30),
                        v("Đen", "256GB", 21990000, 18),
                        v("Trắng", "128GB", 18990000, 25),
                        v("Trắng", "256GB", 21990000, 15),
                        v("Hồng Sen", "128GB", 18990000, 20),
                        v("Hồng Sen", "256GB", 21990000, 12)))
                .build(),

            // 9. Xiaomi 15 Ultra
            Product.builder()
                .name("Xiaomi 15 Ultra")
                .brand("Xiaomi")
                .categoryId(cameraId)
                .price(22990000)
                .originalPrice(24990000.0)
                .image("https://fdn2.gsmarena.com/vv/pics/xiaomi/xiaomi-15-ultra-1.jpg")
                .rating(4.6)
                .badge("Sale")
                .specs("Snapdragon 8 Elite · Leica Camera 200MP · Sạc 90W · IP68")
                .stock(0)
                .variants(
                    List.of(
                        v("Đen", "256GB", 22990000, 25),
                        v("Đen", "512GB", 27990000, 15),
                        v("Trắng", "256GB", 22990000, 20),
                        v("Trắng", "512GB", 27990000, 12)))
                .build(),

            // 10. Xiaomi 15
            Product.builder()
                .name("Xiaomi 15")
                .brand("Xiaomi")
                .categoryId(flagshipId)
                .price(16990000)
                .originalPrice(18990000.0)
                .image("https://fdn2.gsmarena.com/vv/pics/xiaomi/xiaomi-15-1.jpg")
                .rating(4.5)
                .specs("Snapdragon 8 Elite · Leica Camera 50MP · Sạc 90W · IP68")
                .stock(0)
                .variants(
                    List.of(
                        v("Đen", "256GB", 16990000, 30),
                        v("Đen", "512GB", 20990000, 18),
                        v("Trắng", "256GB", 16990000, 25),
                        v("Trắng", "512GB", 20990000, 15),
                        v("Xanh", "256GB", 16990000, 20),
                        v("Xanh", "512GB", 20990000, 10)))
                .build(),

            // 11. OPPO Find X8 Pro
            Product.builder()
                .name("OPPO Find X8 Pro")
                .brand("OPPO")
                .categoryId(cameraId)
                .price(23990000)
                .originalPrice(25990000.0)
                .image("https://fdn2.gsmarena.com/vv/pics/oppo/oppo-find-x8-pro-1.jpg")
                .rating(4.5)
                .specs("Dimensity 9400 · Hasselblad Camera · Sạc 80W · IP69")
                .stock(0)
                .variants(
                    List.of(
                        v("Đen", "256GB", 23990000, 20),
                        v("Đen", "512GB", 27990000, 12),
                        v("Trắng", "256GB", 23990000, 18),
                        v("Trắng", "512GB", 27990000, 10)))
                .build(),

            // 12. OnePlus 13
            Product.builder()
                .name("OnePlus 13")
                .brand("OnePlus")
                .categoryId(flagshipId)
                .price(20990000)
                .originalPrice(22990000.0)
                .image("https://fdn2.gsmarena.com/vv/pics/oneplus/oneplus-13-1.jpg")
                .rating(4.6)
                .specs("Snapdragon 8 Elite · Hasselblad Camera · Pin 6000mAh · Sạc 100W")
                .stock(0)
                .variants(
                    List.of(
                        v("Đen Huyền Bí", "256GB", 20990000, 25),
                        v("Đen Huyền Bí", "512GB", 24990000, 15),
                        v("Trắng Tuyết", "256GB", 20990000, 20),
                        v("Trắng Tuyết", "512GB", 24990000, 12)))
                .build(),

            // 13. Vivo X200 Pro
            Product.builder()
                .name("Vivo X200 Pro")
                .brand("Vivo")
                .categoryId(cameraId)
                .price(23990000)
                .originalPrice(25990000.0)
                .image("https://fdn2.gsmarena.com/vv/pics/vivo/vivo-x200-pro-1.jpg")
                .rating(4.5)
                .specs("Dimensity 9400 · ZEISS Camera 200MP · Pin 6000mAh · IP68")
                .stock(0)
                .variants(
                    List.of(
                        v("Đen", "256GB", 23990000, 20),
                        v("Đen", "512GB", 27990000, 12),
                        v("Xanh", "256GB", 23990000, 18),
                        v("Xanh", "512GB", 27990000, 10)))
                .build(),

            // 14. Realme GT7 Pro
            Product.builder()
                .name("Realme GT7 Pro")
                .brand("Realme")
                .categoryId(gamingId)
                .price(14990000)
                .originalPrice(16990000.0)
                .image("https://fdn2.gsmarena.com/vv/pics/realme/realme-gt-7-pro-1.jpg")
                .rating(4.4)
                .badge("Value")
                .specs("Snapdragon 8 Elite · Pin 6500mAh · Sạc 120W · IP69")
                .stock(0)
                .variants(
                    List.of(
                        v("Đen Vũ Trụ", "256GB", 14990000, 30),
                        v("Đen Vũ Trụ", "512GB", 17990000, 20),
                        v("Xám Titan", "256GB", 14990000, 25),
                        v("Xám Titan", "512GB", 17990000, 15)))
                .build(),

            // 15. Samsung Galaxy Z Fold 6
            Product.builder()
                .name("Samsung Galaxy Z Fold 6")
                .brand("Samsung")
                .categoryId(flagshipId)
                .price(44990000)
                .originalPrice(48990000.0)
                .image("https://fdn2.gsmarena.com/vv/pics/samsung/samsung-galaxy-z-fold6-1.jpg")
                .rating(4.7)
                .badge("Premium")
                .specs("Snapdragon 8 Gen 3 · Màn gập 7.6\" · Camera 50MP · IPX8")
                .stock(0)
                .variants(
                    List.of(
                        v("Xanh Navy", "256GB", 44990000, 15),
                        v("Xanh Navy", "512GB", 49990000, 8),
                        v("Bạc", "256GB", 44990000, 12),
                        v("Bạc", "512GB", 49990000, 6),
                        v("Hồng", "256GB", 44990000, 10),
                        v("Hồng", "512GB", 49990000, 5)))
                .build());

    productRepository.saveAll(products);
    System.out.println(
        ">>> Seeded " + saved.size() + " categories and " + products.size() + " products.");
  }
}
