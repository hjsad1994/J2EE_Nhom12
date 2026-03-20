---
noteId: "e79d7670248611f1b27615d81984e2d6"
tags: []

---

# Local Docker Test

Muc tieu:

- Test local bang Docker truoc khi push
- Khong dung `caddy` khi local
- Sau khi push, VPS chi can `git pull` va `docker compose --env-file .env.vps up -d --build`

## 1. Tao file env local

Tu thu muc root:

```bash
cp .env.example .env
```

Neu dung Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

Cap nhat toi thieu cac bien sau trong `.env`:

- `MONGODB_URI`
- `JWT_SECRET`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `CLOUDINARY_CLOUD_NAME`
- `CLOUDINARY_API_KEY`
- `CLOUDINARY_API_SECRET`

## 2. Chay local bang Docker

```bash
docker compose -f docker-compose.yml -f docker-compose.local.yml up -d --build
```

Local se chay 2 service:

- frontend: `http://localhost:4173`
- backend: `http://localhost:8080`

`caddy` se khong duoc bat trong local override.

Mac dinh local dang dung cung database trong `MONGODB_URI`. Neu ban muon tach DB rieng thi sua lai ten database trong `.env`.

## 3. Kiem tra nhanh

```bash
docker compose -f docker-compose.yml -f docker-compose.local.yml ps
docker compose -f docker-compose.yml -f docker-compose.local.yml logs -f backend
docker compose -f docker-compose.yml -f docker-compose.local.yml logs -f frontend
```

## 4. Tat local stack

```bash
docker compose -f docker-compose.yml -f docker-compose.local.yml down
```

## 5. Push va deploy VPS

Sau khi local test on:

```bash
git add .
git commit -m "chore: prepare local and vps docker env flow"
git push
```

Tren VPS:

```bash
cd /opt/veritashop
git pull
docker compose --env-file .env.vps up -d --build
```

## Ghi chu

- Khong commit `.env` hoac `.env.vps`
- File commit len git chi nen la `.env.example` va `.env.vps.example`
- Neu chi sua frontend hoac backend, co the build nhanh hon:

```bash
docker compose -f docker-compose.yml -f docker-compose.local.yml build frontend
docker compose -f docker-compose.yml -f docker-compose.local.yml up -d frontend
```

```bash
docker compose -f docker-compose.yml -f docker-compose.local.yml build backend
docker compose -f docker-compose.yml -f docker-compose.local.yml up -d backend
```
