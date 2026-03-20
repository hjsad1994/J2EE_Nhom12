# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Full-stack mobile phone e-commerce platform (Vietnamese). React 19 + Vite frontend, Spring Boot 3.4.5 + Java 21 backend, MongoDB Atlas database.

- Frontend runs on port **5173** (Vite dev server)
- Backend runs on port **8080** (Spring Boot)
- ABSA (Aspect-Based Sentiment Analysis) service runs on port **9000** (external)

## Commands

### Frontend (`cd frontend/`)
```bash
npm run dev          # Start dev server
npm run build        # TypeScript compile + Vite production build
npm run typecheck    # Type-check only (tsc -b)
npm run lint         # ESLint check
npm run lint:fix     # ESLint auto-fix
npm run format       # Prettier format
npm run test         # Vitest (unit tests)
npm run test:watch   # Vitest watch mode
```

### Backend (`cd nhom12/`)
```bash
./mvnw spring-boot:run    # Start server
./mvnw -B test            # Run tests
./mvnw -B verify          # Full build + tests
```

Backend auto-loads `.env` from project root. Copy `nhom12/.env.example` to `.env` and fill in secrets.

## Pre-commit Checks (required before frontend commits)
```bash
npm run typecheck && npm run lint
```

## Architecture

### Backend — Layered
```
Controller → Service (interface + impl) → Repository (Spring Data) → Model (@Document)
```
- `config/` — Security, MongoDB, Cloudinary, WebSocket, MoMo, Email configuration
- `controller/` — REST endpoints under `/api/*`
- `service/` + `service/impl/` — Business logic
- `model/` — MongoDB documents; `BaseDocument` is the base class; `Role`/`Status` are enums
- `repository/` — Spring Data MongoDB interfaces
- `dto/request/` + `dto/response/` — Request/response objects
- `security/` — JWT filter, `CustomUserDetailsService`
- `exception/` — `GlobalExceptionHandler` for centralized error handling
- `mapper/` — Entity ↔ DTO conversions

**DI convention**: always use `@RequiredArgsConstructor` (constructor injection), never `@Autowired` field injection.

### Frontend — Feature-Based
```
src/
├── features/<name>/     # Self-contained feature modules
│   ├── components/      # Feature UI
│   ├── hooks/           # Custom hooks
│   ├── services/        # API calls
│   ├── stores/          # Zustand state
│   ├── types/           # TypeScript types
│   └── index.ts         # Barrel export
├── pages/               # Route-level pages
├── components/ui/       # Shared design system primitives
├── components/layout/   # Navbar, Footer, etc.
├── store/               # Global Zustand (cart, wishlist, auth)
├── router/              # React Router config + route guards
└── api/                 # Axios client + endpoint definitions
```

**State management split:**
- **Server state** (API data, caching, mutations): TanStack Query
- **Client state** (cart, wishlist, auth, UI): Zustand

API proxy in dev: `/api` → `:8080`, `/absa-api` → `:9000` (configured in `vite.config.ts`).

## Key Integrations
- **Auth**: JWT (stored in localStorage, sent as `Authorization: Bearer <token>`) + Google OAuth2
- **Images**: Cloudinary (uploads handled in backend)
- **Payment**: MoMo Vietnamese e-wallet (sandbox)
- **Email**: Gmail SMTP
- **Real-time**: WebSocket via STOMP.js
- **ABSA**: External Python service at port 9000 for review sentiment analysis

## Creating an Admin User
Register normally via the UI, then in MongoDB Atlas change the user document's `role` field to `"ADMIN"`.

## Boundaries
- Do not commit `.env` files
- Do not force-push `main`
- Do not edit `dist/` or `target/` directories
- Ask before making schema changes or adding new API endpoints
