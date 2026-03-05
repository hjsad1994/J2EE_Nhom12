package nhom12.example.nhom12.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nhom12.example.nhom12.model.Category;
import nhom12.example.nhom12.model.Product;
import nhom12.example.nhom12.repository.CategoryRepository;
import nhom12.example.nhom12.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "test"})
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;

  @Override
  public void run(String... args) {
    if (categoryRepository.count() > 0 || productRepository.count() > 0) {
      return;
    }

    // Seed categories
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

    List<Category> savedCategories = categoryRepository.saveAll(categories);
    String flagshipId = savedCategories.get(0).getId();
    String midRangeId = savedCategories.get(1).getId();
    String gamingId = savedCategories.get(2).getId();
    String cameraId = savedCategories.get(3).getId();

    // Seed products
    List<Product> products =
        List.of(
            Product.builder()
                .name("iPhone 16 Pro Max")
                .brand("Apple")
                .categoryId(flagshipId)
                .price(34990000)
                .originalPrice(37990000.0)
                .image("https://fdn2.gsmarena.com/vv/pics/apple/apple-iphone-16-pro-max-1.jpg")
                .rating(4.9)
                .badge("Hot")
                .specs("256GB · Titanium · A18 Pro")
                .build(),
            Product.builder()
                .name("Samsung Galaxy S25 Ultra")
                .brand("Samsung")
                .categoryId(flagshipId)
                .price(33990000)
                .originalPrice(36990000.0)
                .image("https://fdn2.gsmarena.com/vv/pics/samsung/samsung-galaxy-s25-ultra-1.jpg")
                .rating(4.8)
                .badge("New")
                .specs("256GB · Titanium · Snapdragon 8 Elite")
                .build(),
            Product.builder()
                .name("Google Pixel 9 Pro")
                .brand("Google")
                .categoryId(cameraId)
                .price(26990000)
                .image("https://fdn2.gsmarena.com/vv/pics/google/google-pixel-9-pro-1.jpg")
                .rating(4.7)
                .specs("128GB · AI Camera · Tensor G4")
                .build(),
            Product.builder()
                .name("Xiaomi 15 Ultra")
                .brand("Xiaomi")
                .categoryId(cameraId)
                .price(23990000)
                .originalPrice(25990000.0)
                .image("https://fdn2.gsmarena.com/vv/pics/xiaomi/xiaomi-15-ultra-1.jpg")
                .rating(4.6)
                .badge("Sale")
                .specs("512GB · Leica Camera · Snapdragon 8 Elite")
                .build(),
            Product.builder()
                .name("OPPO Find X8 Pro")
                .brand("OPPO")
                .categoryId(cameraId)
                .price(24990000)
                .image("https://fdn2.gsmarena.com/vv/pics/oppo/oppo-find-x8-pro-1.jpg")
                .rating(4.5)
                .specs("256GB · Hasselblad Camera · Dimensity 9400")
                .build(),
            Product.builder()
                .name("iPhone 16")
                .brand("Apple")
                .categoryId(flagshipId)
                .price(24990000)
                .originalPrice(26990000.0)
                .image("https://fdn2.gsmarena.com/vv/pics/apple/apple-iphone-16-1.jpg")
                .rating(4.7)
                .specs("128GB · A18 chip · Dynamic Island")
                .build(),
            Product.builder()
                .name("Samsung Galaxy A56 5G")
                .brand("Samsung")
                .categoryId(midRangeId)
                .price(10990000)
                .originalPrice(12990000.0)
                .image("https://fdn2.gsmarena.com/vv/pics/samsung/samsung-galaxy-a55-5g-1.jpg")
                .rating(4.3)
                .badge("Best Seller")
                .specs("128GB · Exynos 1580 · OIS Camera")
                .build(),
            Product.builder()
                .name("OnePlus 13")
                .brand("OnePlus")
                .categoryId(flagshipId)
                .price(22990000)
                .image("https://fdn2.gsmarena.com/vv/pics/oneplus/oneplus-13-1.jpg")
                .rating(4.6)
                .specs("256GB · Snapdragon 8 Elite · 6000mAh")
                .build(),
            Product.builder()
                .name("Vivo X200 Pro")
                .brand("Vivo")
                .categoryId(cameraId)
                .price(24990000)
                .image("https://fdn2.gsmarena.com/vv/pics/vivo/vivo-x200-pro-1.jpg")
                .rating(4.5)
                .specs("256GB · ZEISS Camera · Dimensity 9400")
                .build(),
            Product.builder()
                .name("Realme GT7 Pro")
                .brand("Realme")
                .categoryId(gamingId)
                .price(16990000)
                .originalPrice(18990000.0)
                .image("https://fdn2.gsmarena.com/vv/pics/realme/realme-gt-7-pro-1.jpg")
                .rating(4.4)
                .badge("Value")
                .specs("256GB · Snapdragon 8 Elite · IP69")
                .build());

    productRepository.saveAll(products);
    System.out.println(
        ">>> Seeded "
            + savedCategories.size()
            + " categories and "
            + products.size()
            + " products.");
  }
}
