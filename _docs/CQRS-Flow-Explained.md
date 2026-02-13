# 🔄 LUỒNG CQRS GIẢI THÍCH CHI TIẾT

## 📖 Tổng quan
CQRS (Command Query Responsibility Segregation) là pattern tách biệt **ghi dữ liệu** (Command) và **đọc dữ liệu** (Query). Hãy cùng xem luồng hoạt động từ lúc bạn gọi API đến khi nhận kết quả.

---

## 🎯 LUỒNG 1: TẠO MỚI SÁCH (COMMAND SIDE)

### 📝 Ví dụ thực tế:
```json
POST http://localhost:9001/api/v1/books
Body: {
  "name": "Java Book 1",
  "author": "nvminh162"
}
```

### 🔄 Chi tiết từng bước:

#### **BƯỚC 1: Controller nhận request** 
📍 File: `BookCommandController.java`

```java
@PostMapping
public String addBook(@RequestBody BookRequestModel model) {
    // Tạo Command object từ request
    CreateBookCommand command = CreateBookCommand.builder()
            .id(UUID.randomUUID().toString())  // Tạo ID tự động
            .name(model.getName())              // "Java Book 1"
            .author(model.getAuthor())          // "nvminh162"
            .isReady(true)
            .build();
    
    // Gửi command và chờ kết quả
    return commandGateway.sendAndWait(command);
}
```

**💡 Giải thích:**
- Controller nhận JSON từ client
- Chuyển đổi thành `CreateBookCommand` object
- `CommandGateway` là cổng giao tiếp với Axon Framework
- `sendAndWait()` = gửi lệnh và đợi xử lý xong

---

#### **BƯỚC 2: Aggregate xử lý Command**
📍 File: `BookAggregate.java`

```java
@Aggregate  // Đây là Domain Model chứa business logic
public class BookAggregate {
    
    @AggregateIdentifier
    String id;
    String name;
    String author;
    Boolean isReady;

    @CommandHandler  // ← Axon tự động gọi method này khi có CreateBookCommand
    public BookAggregate(CreateBookCommand command) {
        // 1. Có thể validate business rules ở đây
        // if (command.getName().isEmpty()) throw new Exception();
        
        // 2. Tạo Event (không trực tiếp lưu DB!)
        BookCreatedEvent event = new BookCreatedEvent();
        BeanUtils.copyProperties(command, event);
        
        // 3. Apply event (phát tán sự kiện)
        AggregateLifecycle.apply(event);
    }
    
    @EventSourcingHandler  // ← Tự động gọi khi event được apply
    public void on(BookCreatedEvent event) {
        // Cập nhật state của Aggregate
        this.id = event.getId();
        this.name = event.getName();
        this.author = event.getAuthor();
        this.isReady = event.getIsReady();
    }
}
```

**💡 Giải thích:**
- `@CommandHandler`: Xử lý lệnh, validate business rules
- Không lưu trực tiếp vào DB, mà tạo **Event**
- `AggregateLifecycle.apply(event)`: Phát tán event ra Event Bus
- `@EventSourcingHandler`: Cập nhật trạng thái của Aggregate

**🤔 Tại sao không lưu trực tiếp DB?**
- Event Sourcing: Lưu lại lịch sử thay đổi (audit trail)
- Các service khác có thể lắng nghe event này
- Có thể rebuild lại state từ các events

---

#### **BƯỚC 3: Event Handler lưu vào Database**
📍 File: `BookEventsHandler.java`

```java
@Component
public class BookEventsHandler {
    
    BookRepository bookRepository;

    @EventHandler  // ← Lắng nghe event BookCreatedEvent
    public void on(BookCreatedEvent event) {
        // Chuyển Event → Entity
        Book book = new Book();
        BeanUtils.copyProperties(event, book);
        
        // LƯU VÀO DATABASE ở đây!
        bookRepository.save(book);
    }
}
```

**💡 Giải thích:**
- `@EventHandler`: Lắng nghe event từ Event Bus
- Khi có `BookCreatedEvent`, tự động lưu vào DB
- Đây là nơi **duy nhất** thực sự ghi vào database

---

#### **BƯỚC 4: Trả kết quả về Controller**

```java
return commandGateway.sendAndWait(command);  // Trả về ID của book
// Response: "0a977fd5-b39e-4ed3-b833-8fedc698e936"
```

**💡 Giải thích:**
- `sendAndWait()` đợi cho đến khi Event được xử lý xong
- Trả về ID của Book vừa tạo
- Client nhận được response 200 OK với book ID

---

## 📊 SƠ ĐỒ LUỒNG COMMAND (CREATE BOOK)

```
┌─────────┐
│ Client  │
│ (POST)  │
└────┬────┘
     │ 1. HTTP Request: {"name": "Java Book 1", "author": "nvminh162"}
     ↓
┌─────────────────────────┐
│ BookCommandController   │
│ @PostMapping            │
└────────┬────────────────┘
     │ 2. Tạo CreateBookCommand
     ↓
┌─────────────────────────┐
│ CommandGateway          │
│ sendAndWait()           │
└────────┬────────────────┘
     │ 3. Gửi Command đến Aggregate
     ↓
┌─────────────────────────┐
│ BookAggregate           │
│ @CommandHandler         │
│ - Validate logic        │
│ - Tạo BookCreatedEvent  │
│ - Apply event           │
└────────┬────────────────┘
     │ 4. Phát tán Event ra Event Bus
     ↓
┌─────────────────────────┐
│ Event Bus (Axon)        │
└────────┬────────────────┘
     │ 5. Event được broadcast
     ├─────────────────────┬───────────────────────┐
     ↓                     ↓                       ↓
┌──────────────┐  ┌──────────────────┐  ┌─────────────────┐
│ Aggregate    │  │ EventHandler     │  │ Query Side      │
│ @EventSourcing│ │ (BookEventsHandler)│ │ Projection     │
│ Cập nhật state│ │ → LƯU VÀO DB!    │  │ (sẽ giải thích) │
└──────────────┘  └──────────────────┘  └─────────────────┘
                       │
                       ↓
                  ┌─────────────┐
                  │ Database    │
                  │ (H2/MySQL)  │
                  └─────────────┘
```

---

## 🔍 LUỒNG 2: TÌM KIẾM/ĐỌC SÁCH (QUERY SIDE)

> ⚠️ **Lưu ý**: Trong code hiện tại của bạn, Query Side **CHƯA ĐƯỢC IMPLEMENT**. 
> Tôi sẽ giải thích cách nó **NÊN HOẠT ĐỘNG**.

### 📝 Ví dụ thực tế:
```json
GET http://localhost:9001/api/v1/books/{bookId}
```

### 🔄 Chi tiết từng bước (khi được implement):

#### **BƯỚC 1: Query Controller nhận request**
📍 File: `BookQueryController.java` (CHƯA CÓ - cần tạo)

```java
@RestController
@RequestMapping("/api/v1/books")
public class BookQueryController {
    
    QueryGateway queryGateway;
    
    @GetMapping("/{bookId}")
    public BookResponseModel getBookById(@PathVariable String bookId) {
        // Tạo Query object
        GetBookByIdQuery query = new GetBookByIdQuery(bookId);
        
        // Gửi query và nhận kết quả
        return queryGateway.query(query, BookResponseModel.class).join();
    }
    
    @GetMapping
    public List<BookResponseModel> getAllBooks() {
        GetAllBooksQuery query = new GetAllBooksQuery();
        return queryGateway.query(query, 
            ResponseTypes.multipleInstancesOf(BookResponseModel.class)).join();
    }
}
```

---

#### **BƯỚC 2: Projection xử lý Query**
📍 File: `BookProjection.java` (CHƯA CÓ - cần tạo)

```java
@Component
public class BookProjection {
    
    BookRepository bookRepository;
    
    @QueryHandler  // ← Axon tự động gọi khi có GetBookByIdQuery
    public BookResponseModel handle(GetBookByIdQuery query) {
        // TÌM TRONG DATABASE
        Book book = bookRepository.findById(query.getBookId())
            .orElseThrow(() -> new BookNotFoundException());
        
        // Chuyển Entity → Response Model
        return BookResponseModel.builder()
            .id(book.getId())
            .name(book.getName())
            .author(book.getAuthor())
            .isReady(book.getIsReady())
            .build();
    }
    
    @QueryHandler
    public List<BookResponseModel> handle(GetAllBooksQuery query) {
        return bookRepository.findAll().stream()
            .map(book -> BookResponseModel.builder()
                .id(book.getId())
                .name(book.getName())
                .author(book.getAuthor())
                .isReady(book.getIsReady())
                .build())
            .collect(Collectors.toList());
    }
}
```

**💡 Giải thích:**
- `@QueryHandler`: Xử lý truy vấn đọc dữ liệu
- Đọc trực tiếp từ Database (không qua Event)
- Có thể tối ưu (cache, denormalize data...)

---

#### **BƯỚC 3: Trả kết quả về Client**

```java
// Response:
{
  "id": "0a977fd5-b39e-4ed3-b833-8fedc698e936",
  "name": "Java Book 1",
  "author": "nvminh162",
  "isReady": true
}
```

---

## 📊 SƠ ĐỒ LUỒNG QUERY (GET BOOK)

```
┌─────────┐
│ Client  │
│ (GET)   │
└────┬────┘
     │ 1. HTTP GET /api/v1/books/{bookId}
     ↓
┌─────────────────────────┐
│ BookQueryController     │
│ @GetMapping             │
└────────┬────────────────┘
     │ 2. Tạo GetBookByIdQuery
     ↓
┌─────────────────────────┐
│ QueryGateway            │
│ query()                 │
└────────┬────────────────┘
     │ 3. Gửi Query đến Projection
     ↓
┌─────────────────────────┐
│ BookProjection          │
│ @QueryHandler           │
│ - Tìm trong DB          │
│ - Map sang Response     │
└────────┬────────────────┘
     │ 4. Đọc từ Database
     ↓
┌─────────────────────────┐
│ Database                │
│ SELECT * FROM book      │
└────────┬────────────────┘
     │ 5. Trả dữ liệu
     ↓
┌─────────────────────────┐
│ Client nhận Response    │
│ {"id": "...", ...}      │
└─────────────────────────┘
```

---

## 🎭 SO SÁNH COMMAND vs QUERY

| Khía cạnh | **COMMAND** (Ghi) | **QUERY** (Đọc) |
|-----------|-------------------|-----------------|
| **Mục đích** | Thay đổi dữ liệu | Đọc dữ liệu |
| **HTTP Method** | POST, PUT, DELETE | GET |
| **Controller** | BookCommandController | BookQueryController |
| **Object** | CreateBookCommand | GetBookByIdQuery |
| **Gateway** | CommandGateway | QueryGateway |
| **Handler** | @CommandHandler (Aggregate) | @QueryHandler (Projection) |
| **Event?** | ✅ Tạo Event | ❌ Không tạo Event |
| **Database** | Ghi qua EventHandler | Đọc trực tiếp |
| **Response** | ID hoặc Success | Dữ liệu đầy đủ |

---

## 🔗 CẢ HAI LUỒNG KẾT NỐI VỚI NHAU NHƯ THẾ NÀO?

```
COMMAND SIDE                    EVENT BUS                    QUERY SIDE
============                    =========                    ==========

1. POST /books
   ↓
2. CreateBookCommand
   ↓
3. BookAggregate
   @CommandHandler
   ↓
4. BookCreatedEvent ────────→ Event Bus ─────────→ BookProjection
   ↓                                                  @EventHandler
5. BookEventsHandler                                  Cập nhật Read Model
   @EventHandler                                      (Nếu có separate DB)
   ↓
6. LƯU VÀO DB
   ↓
7. Response: bookId

                                                    8. GET /books/{id}
                                                       ↓
                                                    9. GetBookByIdQuery
                                                       ↓
                                                    10. BookProjection
                                                        @QueryHandler
                                                        ↓
                                                    11. ĐỌC TỪ DB
                                                        ↓
                                                    12. Response: book data
```

---

## 💡 TẠI SAO CẦN CQRS?

### ❌ **Cách truyền thống (không dùng CQRS):**
```java
@RestController
public class BookController {
    
    @PostMapping("/books")
    public Book createBook(@RequestBody Book book) {
        // Validate
        // Save trực tiếp vào DB
        return bookRepository.save(book);
    }
    
    @GetMapping("/books/{id}")
    public Book getBook(@PathVariable String id) {
        return bookRepository.findById(id);
    }
}
```
**Vấn đề:**
- Read và Write dùng chung model
- Khó scale riêng cho read-heavy hoặc write-heavy
- Không có audit trail
- Khó tối ưu performance cho từng loại

---

### ✅ **Với CQRS:**

**Ưu điểm:**
1. **Tách biệt rõ ràng**: Command và Query độc lập
2. **Scale riêng**: 
   - Read-heavy? Scale Query side
   - Write-heavy? Scale Command side
3. **Tối ưu riêng**:
   - Query side: Cache, denormalize data
   - Command side: Focus vào business logic
4. **Event Sourcing**: Lưu lại toàn bộ lịch sử thay đổi
5. **Async Processing**: Event có thể xử lý bất đồng bộ

---

## 🚀 ĐIỀU CHỈNH CHO DỰ ÁN CỦA BẠN

Hiện tại dự án bạn **CHỈ CÓ COMMAND SIDE**. Để hoàn thiện CQRS, cần:

### ✅ Đã có:
- ✅ BookCommandController
- ✅ CreateBookCommand
- ✅ BookAggregate với @CommandHandler
- ✅ BookCreatedEvent
- ✅ BookEventsHandler
- ✅ BookRepository

### ❌ Cần thêm (Query Side):
- ❌ BookQueryController
- ❌ GetBookByIdQuery
- ❌ GetAllBooksQuery
- ❌ BookProjection với @QueryHandler
- ❌ BookResponseModel (Read Model)

---

## 📚 TÓM TẮT LUỒNG CQRS

### **Khi tạo sách (Command):**
```
Client → Controller → Command → Aggregate → Event → EventHandler → DB
                                              ↓
                                         Event Bus
```

### **Khi đọc sách (Query):**
```
Client → Controller → Query → Projection → DB → Response
```

### **Kết nối giữa 2 luồng:**
```
Command Side tạo Event → Event Bus → Query Side lắng nghe → Cập nhật Read Model
```

---

## 🎓 KẾT LUẬN

CQRS giống như **hai con đường riêng biệt**:
- **Command Side**: Đường cao tốc cho xe tải (ghi dữ liệu) - chậm nhưng an toàn, có kiểm soát
- **Query Side**: Đường cao tốc cho xe con (đọc dữ liệu) - nhanh và tối ưu

Chúng kết nối với nhau qua **Event Bus** - như một trạm thu phí chung!

**Event-driven** là chìa khóa: Mọi thay đổi đều tạo Event, và ai cần thì lắng nghe!

