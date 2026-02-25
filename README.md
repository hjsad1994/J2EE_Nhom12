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

| Tính năng           | Mô tả                                                          |
| ------------------- | -------------------------------------------------------------- |
| Đăng ký / Đăng nhập | Xác thực người dùng với Spring Security                        |
| Danh mục sản phẩm   | Duyệt, tìm kiếm và lọc điện thoại                              |
| Giỏ hàng            | Thêm, xóa, cập nhật số lượng sản phẩm                          |
| Danh sách yêu thích | Lưu sản phẩm yêu thích để xem lại sau                          |
| Thanh toán          | Quy trình đặt hàng và lịch sử đơn hàng                         |
| Đánh giá sản phẩm   | Khách hàng đánh giá sao và viết nhận xét                       |
| Phân tích cảm xúc   | Phân loại cảm xúc theo khía cạnh (pin, camera, hiệu năng, ...) |
| Quản trị            | Dashboard quản lý sản phẩm và đơn hàng                         |

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

| Yêu cầu | Phiên bản   |
| ------- | ----------- |
| Node.js | 22 trở lên  |
| Java    | 25 trở lên  |
| MongoDB | 7.x trở lên |
| npm     | 10 trở lên  |

---

## 🚀 Cài Đặt & Chạy

### 1. Clone dự án

```bash
git clone https://github.com/hjsad1994/J2EE_Nhom12.git
cd J2EE_Nhom12
```

### 2. Cài đặt & chạy Backend

```bash
cd nhom12

# Chạy server (cổng 8080)
./mvnw spring-boot:run
```

> Đảm bảo MongoDB đang chạy trước khi khởi động backend.
> Cấu hình kết nối MongoDB tại `nhom12/src/main/resources/application.properties`.

### 3. Cài đặt & chạy Frontend

```bash
cd frontend

# Cài đặt dependencies
npm install

# Chạy dev server (cổng 5173, proxy /api → localhost:8080)
npm run dev
```

### 4. Truy cập ứng dụng

- **Frontend:** [http://localhost:5173](http://localhost:5173)
- **Backend API:** [http://localhost:8080/api](http://localhost:8080/api)

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

### Người dùng

| Method | Endpoint          | Mô tả                    |
| ------ | ----------------- | ------------------------ |
| POST   | `/api/users`      | Tạo tài khoản mới        |
| GET    | `/api/users/{id}` | Lấy thông tin người dùng |

> Các endpoint khác sẽ được bổ sung trong các phase tiếp theo (sản phẩm, giỏ hàng, đơn hàng, đánh giá).

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
| 1   | Huỳnh Gia Bửu |
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
