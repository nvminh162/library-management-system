Dưới đây là **giải thích luồng và nguyên nhân dẫn tới lỗi SSH “handshake failed: ssh: unable to authenticate”** mà bạn hỏi trong liên kết Stack Overflow đó 👇 ([Stack Overflow][1])

---

## 🔐 Tóm tắt luồng SSH trong GitHub Actions

Khi một workflow GitHub Actions muốn **kết nối SSH tới server remote** (để deploy, chạy lệnh…), các bước chính diễn ra như sau:

1. **Workflow GitHub chạy** → ví dụ dùng action như `appleboy/ssh-action` để SSH.
2. **GitHub Actions lấy khóa SSH riêng tư (private key)** từ secret bạn định nghĩa (ví dụ `${{ secrets.SSH_PRIVATE_KEY }}`).
3. Client Actions cố gắng **thiết lập kết nối tới server** qua SSH (TCP 22).
4. Server SSH phản hồi và yêu cầu **xác thực**.
5. Client gửi **phương thức xác thực** (thường là publickey).
6. Server kiểm tra xem publickey đó có hợp lệ không → nếu đúng, nhận kết nối.
7. Nếu sai hoặc không có khóa hợp lệ → lỗi **“handshake failed: unable to authenticate”**. ([GitHub][2])

---

## 📌 Vì sao lỗi này xảy ra?

Lỗi này là **một lỗi xác thực SSH** — tức là khi client (GitHub Actions) cố gắng xác thực với server, server không chấp nhận bất kỳ phương thức nào nên từ chối kết nối. Các nguyên nhân phổ biến:

### ✅ 1. GitHub Action không dùng đúng private key

* Bạn đưa vào secret **private key không đúng** với public key đã thêm vào server (`~/.ssh/authorized_keys`).
* Ví dụ: bạn dùng private key của máy chủ thay vì private key từ máy phát triển. ([Stack Overflow][3])

👉 Private và public key phải là **một cặp** tương ứng.

---

### ✅ 2. Public key chưa được thêm đúng vào server

* Bạn có thể đã **chỉ thêm private key lên server hoặc sai vị trí ghi public key**.
* Phải thêm public key vào `~/.ssh/authorized_keys` trên server để SSH chấp nhận khóa đó. ([Stack Overflow][3])

---

### ✅ 3. Quyền hạn file/directory SSH trên server sai

SSH server yêu cầu:

* `~/.ssh` có quyền `700`
* `authorized_keys` có quyền `600`

Nếu sai, server sẽ **bỏ qua public key** dẫn tới lỗi xác thực. ([GitHub][4])

---

### ✅ 4. Thuật toán khóa không được server chấp nhận

Một số server (nhất là Ubuntu mới) **loại bỏ hỗ trợ `ssh-rsa` theo mặc định** — nếu public key bạn dùng là rsa mà server không chấp nhận, SSH handshake sẽ fail. ([woozxn][5])

---

### ✅ 5. Không bật xác thực bằng public key đúng trong server

Trong file `/etc/ssh/sshd_config`, cần đảm bảo:

```
PubkeyAuthentication yes
```

Nếu không, server sẽ từ chối mọi xác thực public key. ([woozxn][5])

---

## 🔍 Thông điệp lỗi có ý nghĩa gì?

Ví dụ lỗi:

```
ssh: handshake failed: ssh: unable to authenticate, attempted methods [none publickey], no supported methods remain
```

📌 Nghĩa là:

* Client đã thử tất cả phương thức xác thực có thể (`none`, `publickey`)
* Nhưng **server từ chối tất cả** → không còn phương thức nào để xác thực → SSH handshake thất bại. ([GitHub][2])

---

## 📌 Luồng SSH xác thực (rút gọn)

1. Client gửi yêu cầu SSH tới server
2. Server gửi challenge (gợi ý phương thức xác thực chấp nhận)
3. Client gửi **public key** ứng với private key được lưu trong GitHub secret
4. Server kiểm tra public key trong `authorized_keys`

   * Nếu **trùng và hợp lệ** → xác thực thành công
   * Nếu **không** → lỗi `unable to authenticate`

---

## 🛠️ Hướng giải quyết chung

Để tránh lỗi này, bạn nên:

✅ Tạo key SSH mới trên máy dev:

```bash
ssh-keygen -t rsa -b 4096 -C "your_email@example.com"
```

✅ Thêm **public key** vào server:

```bash
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
```

✅ Lưu **private key** vào GitHub Secrets (ví dụ tên `SSH_PRIVATE_KEY`)

✅ Trong workflow dùng đúng secret đó.

---

Nếu bạn muốn, mình có thể **mô phỏng chi tiết luồng SSH với ví dụ workflow cụ thể** luôn! (bao gồm file YAML, cách setup key, cách test kết nối) 🚀

[1]: https://stackoverflow.com/questions/71850567/github-actions-workflow-error-ssh-handshake-failed-ssh-unable-to-authenticat "GitHub actions workflow error: ssh: handshake failed: ssh: unable to authenticate, attempted methods [none password], no supported methods remain - Stack Overflow"
[2]: https://github.com/appleboy/ssh-action/issues/297?utm_source=chatgpt.com "ssh: handshake failed: ssh: unable to authenticate ..."
[3]: https://stackoverflow.com/questions/71850567/github-actions-workflow-error-ssh-handshake-failed-ssh-unable-to-authenticat?utm_source=chatgpt.com "GitHub actions workflow error: ssh: handshake failed ..."
[4]: https://github.com/appleboy/ssh-action/issues/251?utm_source=chatgpt.com "ssh: unable to authenticate, attempted methods [none], no ..."
[5]: https://woozxn.tistory.com/3?utm_source=chatgpt.com "Github Action ssh: handshake failed 에러 - woozxn - 티스토리"
