Tôi dùng **Keycloak REST API** qua PowerShell, 2 bước:

**Bước 1** — Lấy admin access token:
```powershell
$token = (Invoke-RestMethod `
    -Uri "http://localhost:8180/realms/master/protocol/openid-connect/token" `
    -Method Post `
    -Body @{
        client_id  = "admin-cli"
        username   = "admin"
        password   = "admin"
        grant_type = "password"
    }).access_token
```

**Bước 2** — Gọi endpoint **regenerate** client secret (POST = tạo secret mới + trả về luôn):
```powershell
$result = Invoke-RestMethod `
    -Uri "http://localhost:8180/admin/realms/libraryx-system/clients/743ddd0e-eec1-45f7-a58b-660a5fa69281/client-secret" `
    -Method Post `
    -Headers @{ Authorization = "Bearer $token" }

$result.value  # → HV6GzNUOPErlTEatZR7l1agsLPL2vwFg
```

---

**Lưu ý quan trọng:**

- Client ID `743ddd0e-...` là **internal UUID** của client `libraryx-system-cid`, lấy từ lần gọi `GET /admin/realms/libraryx-system/clients` trước đó
- `POST /client-secret` vừa **tạo secret mới** vừa trả về — nên secret cũ bị vô hiệu hóa ngay
- Nếu chỉ muốn **đọc** secret hiện tại mà không tạo mới → dùng `GET` thay vì `POST` (nhưng kcadm.sh trả về `**********` ẩn, còn REST API trả về plaintext)