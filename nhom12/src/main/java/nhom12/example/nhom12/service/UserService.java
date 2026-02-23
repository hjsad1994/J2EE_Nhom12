package nhom12.example.nhom12.service;

import nhom12.example.nhom12.dto.request.CreateUserRequest;
import nhom12.example.nhom12.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(String id);

    Page<UserResponse> getAllUsers(Pageable pageable);
}
