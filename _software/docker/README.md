## ✅ Cách 1 — Lọc tag stable trên Docker Hub (chuẩn nhất)

Bạn đang ở trang:

👉 [https://hub.docker.com/_/maven/tags](https://hub.docker.com/_/maven/tags)

Làm như sau:

### Bước 1

Nhìn danh sách tag.

### Bước 2

**Loại bỏ các tag không stable** (rất quan trọng):

| Loại tag   | Nghĩa             | Stable không |
| ---------- | ----------------- | ------------ |
| `rc`       | release candidate | ❌            |
| `alpha`    | thử nghiệm sớm    | ❌            |
| `beta`     | thử nghiệm        | ❌            |
| `snapshot` | dev build         | ❌            |

Ví dụ ❌ không stable:

```
4.0.0-rc-5
```

---

### Bước 3

Tìm version **số lớn nhất nhưng KHÔNG có rc / alpha / beta**

Hiện tại (thời điểm bạn chụp màn hình):

👉 Maven stable mới nhất = **3.9.12**

---

### Bước 4

Chọn Java version bạn cần

Ví dụ:

| Java    | Tag nên dùng                |
| ------- | --------------------------- |
| Java 17 | `3.9.12-eclipse-temurin-17` |
| Java 21 | `3.9.12-eclipse-temurin-21` |

---

## ✅ Kết quả cho bạn (Java 21)

👉 Stable mới nhất:

```dockerfile
FROM maven:3.9.12-eclipse-temurin-21
```

---

## ✅ Cách 2 — Xem “Supported tags” trong Overview

Trang:

👉 [https://hub.docker.com/_/maven](https://hub.docker.com/_/maven)

Kéo xuống phần:

```
Supported tags and respective Dockerfile links
```

Docker official maintainers chỉ liệt kê các tag **được support chính thức** → thường là stable.

---

## ✅ Cách 3 — Pull thử (cách dev hay dùng)

```bash
docker pull maven:3.9.12-eclipse-temurin-21
```

Pull OK → tồn tại → stable.

---

## 🧠 Quy tắc chọn version Docker image (rất quan trọng)

1️⃣ Không dùng `latest`
2️⃣ Không dùng `rc / alpha / beta`
3️⃣ Dùng version số lớn nhất stable
4️⃣ Chọn đúng Java version

---

## 🔥 Cheat sheet cho Spring Boot (2025)

| Bạn dùng            | Docker image                    |
| ------------------- | ------------------------------- |
| Spring Boot Java 17 | maven:3.9.12-eclipse-temurin-17 |
| Spring Boot Java 21 | maven:3.9.12-eclipse-temurin-21 |

---