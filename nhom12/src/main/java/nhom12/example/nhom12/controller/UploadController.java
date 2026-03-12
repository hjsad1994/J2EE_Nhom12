package nhom12.example.nhom12.controller;

import lombok.RequiredArgsConstructor;
import nhom12.example.nhom12.dto.response.ApiResponse;
import nhom12.example.nhom12.service.CloudinaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

  private final CloudinaryService cloudinaryService;

  @PostMapping("/image")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<String>> uploadImage(
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "folder", defaultValue = "products") String folder) {
    String url = cloudinaryService.upload(file, folder);
    return ResponseEntity.ok(ApiResponse.success(url, "Image uploaded successfully"));
  }
}
