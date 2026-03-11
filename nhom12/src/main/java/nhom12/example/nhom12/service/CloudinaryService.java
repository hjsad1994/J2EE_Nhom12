package nhom12.example.nhom12.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import nhom12.example.nhom12.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

  private static final String[] ALLOWED_TYPES = {
    "image/jpeg", "image/png", "image/webp", "image/gif"
  };

  private final Cloudinary cloudinary;

  public String upload(MultipartFile file, String folder) {
    validateFile(file);
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> result =
          cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("folder", folder));
      return (String) result.get("secure_url");
    } catch (IOException e) {
      throw new BadRequestException("Upload ảnh thất bại: " + e.getMessage());
    }
  }

  public void delete(String publicId) {
    try {
      cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    } catch (IOException e) {
      // Log but don't throw — deletion failure is not critical
    }
  }

  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BadRequestException("File không được để trống");
    }
    if (file.getSize() > MAX_FILE_SIZE) {
      throw new BadRequestException("File không được vượt quá 10MB");
    }
    String contentType = file.getContentType();
    boolean allowed = false;
    for (String type : ALLOWED_TYPES) {
      if (type.equals(contentType)) {
        allowed = true;
        break;
      }
    }
    if (!allowed) {
      throw new BadRequestException(
          "Chỉ hỗ trợ ảnh định dạng: JPEG, PNG, WebP, GIF");
    }
  }
}
