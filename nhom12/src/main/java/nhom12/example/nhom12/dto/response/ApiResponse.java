package nhom12.example.nhom12.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

  private T data;
  private String message;
  private int status;

  public static <T> ApiResponse<T> success(T data, String message) {
    return ApiResponse.<T>builder().data(data).message(message).status(200).build();
  }

  public static <T> ApiResponse<T> created(T data, String message) {
    return ApiResponse.<T>builder().data(data).message(message).status(201).build();
  }
}
