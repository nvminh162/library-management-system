Đây là lệnh tương đương cho Ubuntu terminal:

**Bước 1** — Lấy admin access token:
```bash
TOKEN=$(curl -s -X POST "http://localhost:8180/realms/master/protocol/openid-connect/token" \
  -d "client_id=admin-cli" \
  -d "username=admin" \
  -d "password=admin" \
  -d "grant_type=password" | jq -r '.access_token')
```

**Bước 2** — Regenerate client secret:
```bash
curl -s -X POST \
  "http://localhost:8180/admin/realms/libraryx-system/clients/743ddd0e-eec1-45f7-a58b-660a5fa69281/client-secret" \
  -H "Authorization: Bearer $TOKEN" | jq -r '.value'
```

> Cần cài `jq` nếu chưa có: `sudo apt install jq`