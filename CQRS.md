Cấu trúc dự án áp dụng pattern **CQRS (Command Query Responsibility Segregation)**

## 📂 Cấu trúc tổng quan

Dự án được chia thành 2 phần chính: **command** (ghi dữ liệu) và **query** (đọc dữ liệu).

### 1️⃣ **Command Side** (Xử lý nghiệp vụ ghi)

```
📁 command/
├── 📁 aggregate      # Domain models chứa business logic
├── 📂 command        # Các lệnh (CreateBook, UpdateBook, DeleteBook...)
├── 🎮 controller     # API endpoints nhận requests ghi
├── 📊 data           # Repository/Data access cho write model
├── 📢 event          # Domain events (BookCreated, BookUpdated...)
└── 📦 model          # DTOs, request/response models
```

**Nhiệm vụ:**
- Xử lý các thao tác thay đổi dữ liệu (Create, Update, Delete)
- Validate business rules
- Phát sinh domain events
- Đảm bảo tính nhất quán của dữ liệu

### 2️⃣ **Query Side** (Xử lý nghiệp vụ đọc)

```
📁 query/
├── 🎮 controller     # API endpoints nhận requests đọc
├── 📦 model          # Read models (được tối ưu cho truy vấn)
├── 📁 projection     # Xử lý events để cập nhật read models
└── 📋 queries        # Các query objects (GetBookById, SearchBooks...)
```

**Nhiệm vụ:**
- Xử lý các thao tác đọc dữ liệu
- Không chứa business logic phức tạp
- Tối ưu hóa cho performance (denormalized data)
- Lắng nghe events từ command side để cập nhật

### 3️⃣ **BookServiceApplication**
- Class chính khởi động ứng dụng Spring Boot

## 🔄 Luồng hoạt động

**Write Flow (Command):**
```
Client → Controller → Command → Aggregate → Event → Data Store
                                              ↓
                                         Event Bus
```

**Read Flow (Query):**
```
Client → Controller → Query → Model → Response
                        ↑
                   Projection (lắng nghe events)
```

## ✅ Ưu điểm của cấu trúc này

1. **Separation of Concerns**: Tách biệt rõ ràng read/write
2. **Scalability**: Scale độc lập cho read/write operations
3. **Performance**: Tối ưu riêng cho từng loại truy vấn
4. **Flexibility**: Dễ dàng thay đổi data models cho từng side
5. **Event-Driven**: Hỗ trợ event sourcing và async processing

## 🎯 Use case phù hợp

Pattern này phù hợp với hệ thống có:
- Tỷ lệ read/write không cân bằng
- Yêu cầu performance cao cho queries
- Cần audit trail đầy đủ
- Domain logic phức tạp

```
 Client (Postman / Browser)
   │
   │  POST /api/v1/books  {"name":"Java Book","author":"nvminh162"}
   ▼
┌──────────────────────────────────────────────────────────────────┐
│ [1] BookCommandController                                        │
│     Nhận request → Tạo CreateBookCommand → Gửi qua CommandGateway│
└──────────────────────┬───────────────────────────────────────────┘
                       │  commandGateway.sendAndWait(command)
                       ▼
┌──────────────────────────────────────────────────────────────────┐
│ [2] Axon Command Bus  (framework tự xử lý, không có code)       │
│     Nhận command → Tìm @CommandHandler phù hợp → Gọi Aggregate  │
└──────────────────────┬───────────────────────────────────────────┘
                       ▼
┌──────────────────────────────────────────────────────────────────┐
│ [3] BookAggregate (@CommandHandler)                               │
│     Validate logic → Tạo Event → AggregateLifecycle.apply(event) │
│                                                                  │
│     apply(event) sẽ làm 3 việc song song:                        │
│       ├─ a. Gọi @EventSourcingHandler → cập nhật state Aggregate │
│       ├─ b. Lưu event vào Event Store (Axon tự động)             │
│       └─ c. Publish event ra Event Bus                           │
└──────────────────────┬───────────────────────────────────────────┘
                       │  Event Bus broadcast
                       ▼
┌──────────────────────────────────────────────────────────────────┐
│ [4] BookEventsHandler (@EventHandler)                             │
│     Lắng nghe event → Tạo JPA Entity → bookRepository.save()     │
│     → INSERT INTO books ...                                      │
└──────────────────────┬───────────────────────────────────────────┘
                       ▼
┌──────────────────────────────────────────────────────────────────┐
│ [5] Response trả về Client                                       │
│     HTTP 200 OK                                                  │
│     Body: "0a977fd5-b39e-4ed3-b833-8fedc698e936" (Book ID)       │
└──────────────────────────────────────────────────────────────────┘
```

```
 Client (Postman / Browser)
   │
   │  GET /api/v1/books
   ▼
┌──────────────────────────────────────────────────────────────────┐
│ [1] BookQueryController                                          │
│     Nhận request → Tạo Query object → Gửi qua QueryGateway      │
└──────────────────────┬───────────────────────────────────────────┘
                       │  queryGateway.query(query, responseType).join()
                       ▼
┌──────────────────────────────────────────────────────────────────┐
│ [2] Axon Query Bus  (framework tự xử lý, không có code)         │
│     Nhận query → Tìm @QueryHandler phù hợp → Gọi Projection    │
└──────────────────────┬───────────────────────────────────────────┘
                       ▼
┌──────────────────────────────────────────────────────────────────┐
│ [3] BookProjection (@QueryHandler)                               │
│     Đọc từ Database → Map sang BookResponseModel → Trả về       │
│                                                                  │
│     bookRepository.findAll()                                     │
│       → SELECT * FROM books                                      │
│       → List<Book> → List<BookResponseModel>                     │
└──────────────────────┬───────────────────────────────────────────┘
                       ▼
┌──────────────────────────────────────────────────────────────────┐
│ [4] Response trả về Client                                       │
│     HTTP 200 OK                                                  │
│     Body: [{"id":"...","name":"Java Book 1","author":"nvminh162", │
│             "isReady":true}, ...]                                 │
└──────────────────────────────────────────────────────────────────┘
```