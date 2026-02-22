Đây là cấu hình **1 broker KRaft mode** (không cần Zookeeper). Kafka cần **nhiều listener** cho các mục đích khác nhau, không phải nhiều broker:

---

**3 listener trên cùng 1 broker:**

| Listener | Port | Dùng cho | Ai kết nối |
|----------|------|----------|------------|
| `PLAINTEXT` | `9092` | Client bên **ngoài** Docker (máy host) | Postman, app local |
| `CONTROLLER` | `9093` | **KRaft** — broker tự bầu leader nội bộ | Chỉ Kafka nội bộ |
| `INTERNAL` | `9094` | Giao tiếp **trong** Docker network | Các service khác trong Docker |

---

**Tại sao `kafka-ui` dùng `kafka:9094` thay vì `9092`?**

```yaml
KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092,INTERNAL://kafka:9094
#                                        ↑                         ↑
#                               máy host truy cập        container truy cập
```

- `9092` được advertise là `localhost:9092` → chỉ đúng khi truy cập từ **máy host**
- Nếu `kafka-ui` (trong Docker) kết nối `kafka:9092` → Kafka trả về địa chỉ `localhost:9092` → **kafka-ui không tìm thấy** vì `localhost` trong container là chính nó
- `9094` được advertise là `kafka:9094` → đúng hostname trong Docker network → **kafka-ui kết nối được**

---

**Tóm lại luồng:**

```
Máy host (app local / Postman test)  →  localhost:9092  →  PLAINTEXT listener
kafka-ui / service trong Docker      →  kafka:9094      →  INTERNAL listener  
Kafka tự bầu leader (KRaft)          →  kafka:9093      →  CONTROLLER listener
```

Cấu hình này là **đúng và chuẩn**.