# java_cuoi_ki

Full-stack e-commerce: React 19 + Vite 7 frontend, Spring Boot 4 + MongoDB backend.

## Structure

```
frontend/        # React 19 + Vite 7 + Tailwind CSS 4 + TypeScript 5.9
nhom12/          # Spring Boot 4.0.3 + Java 25 + MongoDB
.github/         # CI workflows (frontend-ci.yml, backend-ci.yml)
```

## Frontend (`frontend/`)

| Area       | Location             | Notes                          |
| ---------- | -------------------- | ------------------------------ |
| Pages      | `src/pages/`         | Home, Auth, Cart, Products     |
| Components | `src/components/ui/` | ProductCard, Navbar, Footer    |
| State      | `src/store/`         | Zustand (cart, wishlist)       |
| API        | `src/api/`           | Axios client + typed endpoints |
| Types      | `src/types/`         | Shared TS definitions          |

```tsx
// Typical component pattern (src/components/ui/ProductCard.tsx)
export default function ProductCard({ product }: ProductCardProps) {
  const toggleWishlist = useWishlistStore((s) => s.toggle);
  const addToCart = useCartStore((s) => s.addItem);
}
```

## Backend (`nhom12/`)

Layered: Controller → Service (interface/impl) → Repository → Model

| Area        | Location        | Notes                            |
| ----------- | --------------- | -------------------------------- | ------------------------ |
| Controllers | `controller/`   | REST endpoints (`/api/*`)        |
| Services    | `service/impl/` | Business logic implementations   |
| Models      | `model/`        | MongoDB documents + BaseDocument |
| DTOs        | `dto/request    | response/`                       | Request/response objects |
| Security    | `security/`     | CustomUserDetailsService         |
| Exceptions  | `exception/`    | GlobalExceptionHandler           |

```java
// Typical controller pattern (controller/UserController.java)
@RestController @RequestMapping("/api/users") @RequiredArgsConstructor
public class UserController {
  private final UserService userService;
}
```

## Commands

```bash
# Frontend (cd frontend/)
npm run dev          # Vite dev server (proxies /api → localhost:8080)
npm run build        # tsc -b && vite build
npm run typecheck    # tsc -b
npm run lint         # eslint
npm run test         # vitest run

# Backend (cd nhom12/)
./mvnw spring-boot:run    # Run server
./mvnw -B test            # Run tests
./mvnw -B verify          # Full build + test
```

## Boundaries

- **Always:** Run `npm run typecheck` and `npm run lint` before frontend commits
- **Always:** Use `@RequiredArgsConstructor` for DI, never field injection
- **Ask first:** Schema changes to MongoDB models, new API endpoints
- **Never:** Commit `.env` files, force push to `main`, edit `dist/` or `target/`
