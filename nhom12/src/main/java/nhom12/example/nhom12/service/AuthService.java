package nhom12.example.nhom12.service;

import nhom12.example.nhom12.dto.request.CreateUserRequest;
import nhom12.example.nhom12.dto.request.LoginRequest;
import nhom12.example.nhom12.dto.response.AuthResponse;

public interface AuthService {

  AuthResponse register(CreateUserRequest request);

  AuthResponse login(LoginRequest request);
}
