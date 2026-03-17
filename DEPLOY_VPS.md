---
noteId: "0c27f020222e11f1ae369f38972208c2"
tags: []

---

# Deploy VPS with Docker Compose

Muc tieu production:

- Frontend: `https://veritashop.click`
- API: `https://api.veritashop.click`
- Server: `160.250.247.176`
- Services: `frontend`, `backend`, `mongo`, `caddy`

## 1. Chuan bi DNS

Tao cac record DNS:

- `A` `veritashop.click` -> `160.250.247.176`
- `A` `api.veritashop.click` -> `160.250.247.176`
- `CNAME` `www` -> `veritashop.click`

Neu dung Cloudflare:

- Luc cap SSL lan dau, de `DNS only`
- Sau khi site chay on dinh moi bat proxy mau cam

Kiem tra DNS da tro dung chua:

```bash
nslookup veritashop.click
nslookup api.veritashop.click
```

## 2. Dang nhap VPS

```bash
ssh root@160.250.247.176
```

## 3. Cai Docker va Docker Compose

```bash
apt update
apt install -y ca-certificates curl gnupg
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
chmod a+r /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo $VERSION_CODENAME) stable" > /etc/apt/sources.list.d/docker.list
apt update
apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin git
systemctl enable docker
systemctl start docker
docker --version
docker compose version
```

## 4. Mo cong firewall

Neu VPS dang bat `ufw` thi chay:

```bash
ufw allow 22
ufw allow 80
ufw allow 443
ufw reload
ufw status
```

## 5. Clone project len VPS

```bash
cd /opt
git clone <repo-url> veritashop
cd /opt/veritashop
```

Neu ban copy code len bang zip/scp thi van can vao dung thu muc:

```bash
cd /opt/veritashop
```

## 6. Chuan bi file env production

Repo da co san file:

- `.env.vps`
- `.env.vps.example`

Kiem tra:

```bash
ls -la
cat .env.vps
```

Sua file:

```bash
nano .env.vps
```

Ban phai thay cac gia tri nay:

- `JWT_SECRET=please_replace_with_a_long_random_secret_min_32_chars`
- `GOOGLE_CLIENT_ID=your_google_client_id`
- `GOOGLE_CLIENT_SECRET=your_google_client_secret`
- `CLOUDINARY_CLOUD_NAME=your_cloudinary_cloud_name`
- `CLOUDINARY_API_KEY=your_cloudinary_api_key`
- `CLOUDINARY_API_SECRET=your_cloudinary_api_secret`
- `MAIL_USERNAME=your_email@gmail.com`
- `MAIL_PASSWORD=your_app_password`
- `MOMO_PARTNER_CODE=your_momo_partner_code`
- `MOMO_ACCESS_KEY=your_momo_access_key`
- `MOMO_SECRET_KEY=your_momo_secret_key`

Neu chua dung ABSA service tren VPS, tam thoi giu:

```env
ABSA_API_URL=http://host.docker.internal:9000
```

Tao JWT secret nhanh:

```bash
openssl rand -base64 48
```

## 7. Deploy he thong

Build va chay:

```bash
docker compose --env-file .env.vps up -d --build
```

Kiem tra container:

```bash
docker compose --env-file .env.vps ps
```

Ban phai thay 4 service:

- `veritashop-mongo`
- `veritashop-backend`
- `veritashop-frontend`
- `veritashop-caddy`

## 8. Xem log neu co loi

Log reverse proxy:

```bash
docker compose --env-file .env.vps logs -f caddy
```

Log backend:

```bash
docker compose --env-file .env.vps logs -f backend
```

Log frontend:

```bash
docker compose --env-file .env.vps logs -f frontend
```

## 9. Kiem tra website sau deploy

Mo tren trinh duyet:

- `https://veritashop.click`
- `https://api.veritashop.click/api/products`

Neu muon test ngay tren VPS:

```bash
curl -I https://veritashop.click
curl -I https://api.veritashop.click/api/products
```

## 10. Cau hinh Google OAuth

Trong Google Cloud Console:

- Authorized JavaScript origins:
  - `https://veritashop.click`
  - `https://www.veritashop.click`
- Authorized redirect URI:
  - `https://api.veritashop.click/login/oauth2/code/google`

Sau khi doi thong tin OAuth, deploy lai:

```bash
docker compose --env-file .env.vps up -d --build
```

## 11. Lenh quan ly sau nay

Cap nhat code:

```bash
cd /opt/veritashop
git pull
docker compose --env-file .env.vps up -d --build
```

Khoi dong lai:

```bash
docker compose --env-file .env.vps restart
```

Dung he thong:

```bash
docker compose --env-file .env.vps down
```

Xem trang thai:

```bash
docker compose --env-file .env.vps ps
```

## 12. Ghi chu quan trong

- `mongo` dang luu du lieu trong volume Docker, khong mat khi restart container.
- `caddy` tu cap va gia han SSL Let's Encrypt.
- `caddy` proxy duoc ca WebSocket cho endpoint `/ws`.
- Backend khong mo cong `8080` ra ngoai Internet.
- Neu Cloudflare dang proxy va SSL loi, tam thoi chuyen ve `DNS only` de kiem tra.
