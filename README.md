# 📱 Nền Tảng Thương Mại Điện Tử Điện Thoại Di Động

Nền tảng thương mại điện tử full-stack cho điện thoại di động, tích hợp phân tích cảm xúc trên đánh giá của khách hàng. Được xây dựng với **React 19** + **Vite 7** (frontend) và **Spring Boot 4** + **MongoDB** (backend).

> Đồ án cuối kỳ môn Lập trình Java/Web — Nhóm 12

---

## 📋 Mục Lục

- [Tính Năng](#-tính-năng)
- [Công Nghệ Sử Dụng](#-công-nghệ-sử-dụng)
- [Cấu Trúc Dự Án](#-cấu-trúc-dự-án)
- [Yêu Cầu Hệ Thống](#-yêu-cầu-hệ-thống)
- [Cài Đặt & Chạy](#-cài-đặt--chạy)
- [Lệnh Hữu Ích](#-lệnh-hữu-ích)
- [API Endpoints](#-api-endpoints)
- [Đóng Góp](#-đóng-góp)
- [Giấy Phép](#-giấy-phép)

---

## ✨ Tính Năng

| Tính năng              | Mô tả                                                           |
| ---------------------- | --------------------------------------------------------------- |
| Đăng ký / Đăng nhập   | Xác thực bằng username/password với JWT                         |
| Đăng nhập Google       | OAuth2 qua Google, tự động tạo tài khoản                        |
| Danh mục sản phẩm      | Duyệt, tìm kiếm và lọc điện thoại theo category/brand          |
| Chi tiết sản phẩm      | Ảnh, giá, thông số kỹ thuật, sản phẩm liên quan                 |
| Giỏ hàng               | Thêm, xóa, cập nhật số lượng, tính phí ship tự động            |
| Danh sách yêu thích    | Toggle yêu thích, đồng bộ server khi đã đăng nhập              |
| Đặt hàng & Thanh toán  | COD hoặc MoMo (sandbox), điền địa chỉ giao hàng                 |
| Đánh giá sản phẩm      | Viết đánh giá sao, xóa đánh giá của mình, rating tự cập nhật   |
| Hồ sơ cá nhân          | Xem thông tin, đổi mật khẩu, xem lịch sử đơn hàng              |
| Quản trị               | Dashboard quản lý sản phẩm và đơn hàng (role ADMIN)            |

---

## 🛠 Công Nghệ Sử Dụng

### Frontend

| Công nghệ      | Phiên bản | Mô tả                         |
| -------------- | --------- | ----------------------------- |
| React          | 19.2      | Thư viện giao diện người dùng |
| Vite           | 7.3       | Build tool nhanh              |
| TypeScript     | 5.9       | Kiểu dữ liệu tĩnh             |
| Tailwind CSS   | 4.2       | CSS tiện ích                  |
| Zustand        | 5.0       | Quản lý state phía client     |
| TanStack Query | 5.9       | Quản lý state từ server       |
| React Router   | 7.1       | Điều hướng SPA                |
| Axios          | 1.13      | HTTP client                   |
| Motion         | 12.3      | Hiệu ứng animation            |
| Vitest         | 4.0       | Testing framework             |

### Backend

| Công nghệ         | Phiên bản | Mô tả                    |
| ----------------- | --------- | ------------------------ |
| Spring Boot       | 4.0.3     | Framework backend        |
| Java              | 25        | Ngôn ngữ lập trình       |
| MongoDB           | —         | Cơ sở dữ liệu NoSQL      |
| Spring Security   | —         | Bảo mật và xác thực      |
| Spring Validation | —         | Xác thực dữ liệu đầu vào |
| Lombok            | —         | Giảm code boilerplate    |

### CI/CD

- **GitHub Actions** — Tự động kiểm tra frontend và backend trên mỗi push/PR

---

## 📁 Cấu Trúc Dự Án

```
java_cuoi_ki/
├── frontend/                    # Ứng dụng React
│   ├── src/
│   │   ├── api/                 # Axios client & endpoint definitions
│   │   ├── components/          # Component UI dùng chung
│   │   │   ├── layout/          # Navbar, Footer, ...
│   │   │   └── ui/              # ProductCard, Button, ...
│   │   ├── features/            # Module tính năng
│   │   ├── pages/               # Trang: Home, Auth, Cart, Products, ...
│   │   ├── router/              # Cấu hình routing & route guards
│   │   ├── store/               # Zustand stores (giỏ hàng, yêu thích)
│   │   └── types/               # TypeScript type definitions
│   ├── package.json
│   └── vite.config.ts
│
├── nhom12/                      # Ứng dụng Spring Boot
│   └── src/main/java/.../nhom12/
│       ├── config/              # Cấu hình (MongoDB, Security)
│       ├── controller/          # REST controllers (/api/*)
│       ├── dto/                 # Request & Response objects
│       │   ├── request/
│       │   └── response/
│       ├── exception/           # Xử lý lỗi toàn cục
│       ├── mapper/              # Chuyển đổi dữ liệu
│       ├── model/               # MongoDB documents
│       ├── repository/          # Spring Data repositories
│       ├── security/            # Spring Security config
│       └── service/             # Business logic
│           └── impl/            # Triển khai service
│
├── .github/                     # GitHub Actions CI workflows
├── AGENTS.md                    # Quy tắc dự án cho AI agents
└── LICENSE                      # Giấy phép MIT
```

---

## 📌 Yêu Cầu Hệ Thống

| Yêu cầu | Phiên bản  |
| ------- | ---------- |
| Node.js | 18 trở lên |
| Java    | 21 trở lên |
| npm     | 9 trở lên  |

> **Không cần cài MongoDB** — dự án dùng MongoDB Atlas (cloud), đã cấu hình sẵn.

---

## 🚀 Cài Đặt & Chạy

### 1. Clone dự án

```bash
git clone https://github.com/hjsad1994/J2EE_Nhom12.git
cd J2EE_Nhom12
```

### 2. Chạy Backend

```bash
cd nhom12
```

Cần set 2 biến môi trường cho Google OAuth2 (lấy từ Google Cloud Console):

**Windows CMD:**
```cmd
set GOOGLE_CLIENT_ID=your_client_id
set GOOGLE_CLIENT_SECRET=your_client_secret
mvnw spring-boot:run
```

**Windows PowerShell:**
```powershell
$env:GOOGLE_CLIENT_ID="your_client_id"
$env:GOOGLE_CLIENT_SECRET="your_client_secret"
./mvnw spring-boot:run
```

**Linux / macOS:**
```bash
GOOGLE_CLIENT_ID=your_client_id GOOGLE_CLIENT_SECRET=your_client_secret ./mvnw spring-boot:run
```

> Nếu không cần tính năng đăng nhập Google, bỏ qua biến môi trường và chạy thẳng `./mvnw spring-boot:run`.

### 3. Chạy Frontend

Mở terminal mới:

```bash
cd frontend
npm install
npm run dev
```

### 4. Truy cập ứng dụng

| URL | Mô tả |
| --- | ----- |
| http://localhost:5173 | Trang web chính |
| http://localhost:5173/login | Đăng nhập / Đăng ký |
| http://localhost:5173/admin | Trang quản trị (cần role ADMIN) |
| http://localhost:8080/api | Backend API |

### 5. Tạo tài khoản Admin

1. Đăng ký tài khoản thường tại `/login`
2. Vào **MongoDB Atlas** → cluster → collection `users`
3. Tìm user vừa tạo → đổi field `role` từ `USER` thành `ADMIN`
4. Đăng nhập lại → tự động redirect vào `/admin`

---

## 📝 Lệnh Hữu Ích

### Frontend (`cd frontend/`)

| Lệnh                | Mô tả                     |
| ------------------- | ------------------------- |
| `npm run dev`       | Chạy dev server           |
| `npm run build`     | Build production          |
| `npm run typecheck` | Kiểm tra kiểu TypeScript  |
| `npm run lint`      | Kiểm tra linting (ESLint) |
| `npm run test`      | Chạy unit tests (Vitest)  |

### Backend (`cd nhom12/`)

| Lệnh                     | Mô tả                     |
| ------------------------ | ------------------------- |
| `./mvnw spring-boot:run` | Chạy server               |
| `./mvnw -B test`         | Chạy unit tests           |
| `./mvnw -B verify`       | Build đầy đủ + chạy tests |

---

## 🔌 API Endpoints

### Auth (public)

| Method | Endpoint              | Mô tả                |
| ------ | --------------------- | -------------------- |
| POST   | `/api/auth/register`  | Đăng ký tài khoản    |
| POST   | `/api/auth/login`     | Đăng nhập, nhận JWT  |
| GET    | `/oauth2/authorization/google` | Đăng nhập Google |

### Sản phẩm & Danh mục (public)

| Method | Endpoint                  | Mô tả                         |
| ------ | ------------------------- | ----------------------------- |
| GET    | `/api/products`           | Danh sách sản phẩm (phân trang) |
| GET    | `/api/products/{id}`      | Chi tiết sản phẩm             |
| GET    | `/api/categories`         | Danh sách danh mục            |

### Đánh giá (GET public, POST/DELETE cần đăng nhập)

| Method | Endpoint           | Mô tả                      |
| ------ | ------------------ | -------------------------- |
| GET    | `/api/reviews?productId=` | Đánh giá theo sản phẩm |
| POST   | `/api/reviews`     | Tạo đánh giá               |
| DELETE | `/api/reviews/{id}` | Xóa đánh giá của mình     |

### Wishlist (cần đăng nhập)

| Method | Endpoint                    | Mô tả                    |
| ------ | --------------------------- | ------------------------ |
| GET    | `/api/wishlist`             | Danh sách yêu thích      |
| POST   | `/api/wishlist/{productId}` | Toggle thêm/bỏ yêu thích |
| DELETE | `/api/wishlist`             | Xóa toàn bộ yêu thích    |

### Đơn hàng (cần đăng nhập)

| Method | Endpoint              | Mô tả                             |
| ------ | --------------------- | --------------------------------- |
| POST   | `/api/orders`         | Tạo đơn hàng mới                  |
| GET    | `/api/orders/my`      | Lịch sử đơn hàng của mình         |
| GET    | `/api/orders`         | Tất cả đơn hàng (chỉ ADMIN)       |
| PATCH  | `/api/orders/{id}/status` | Cập nhật trạng thái (chỉ ADMIN) |

### Thanh toán MoMo (cần đăng nhập)

| Method | Endpoint                     | Mô tả                         |
| ------ | ---------------------------- | ----------------------------- |
| POST   | `/api/momo/create?orderId=`  | Tạo link thanh toán MoMo      |

### Người dùng (cần đăng nhập)

| Method | Endpoint                | Mô tả                    |
| ------ | ----------------------- | ------------------------ |
| GET    | `/api/users/me`         | Thông tin tài khoản      |
| PUT    | `/api/users/me/password` | Đổi mật khẩu            |

---

## 🤝 Đóng Góp

1. Fork dự án
2. Tạo nhánh tính năng (`git checkout -b feat/ten-tinh-nang`)
3. Commit thay đổi (`git commit -m "feat: mô tả thay đổi"`)
4. Push lên nhánh (`git push origin feat/ten-tinh-nang`)
5. Mở Pull Request

### Quy tắc đóng góp

- Chạy `npm run typecheck` và `npm run lint` trước khi commit frontend
- Sử dụng `@RequiredArgsConstructor` cho dependency injection (không dùng field injection)
- Không commit file `.env` hoặc secrets
- Không force push lên nhánh `main`

---

## 👥 Thành Viên Nhóm 12

| STT | Họ và Tên     |
| --- | ------------- |
| 1   | Trần Tấn Tài  |
| 2   |               |
| 3   |               |
| 4   |               |

> Vui lòng cập nhật danh sách thành viên.

---

## 📄 Giấy Phép

Dự án được phân phối dưới giấy phép [MIT](LICENSE).

---

<p align="center">
  <i>Đồ án cuối kỳ — Nhóm 12 — 2026</i>
</p>
