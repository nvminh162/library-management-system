# 🔄 LUỒNG COMMAND SIDE - GIẢI THÍCH TỪNG DÒNG CODE

## 📖 Tổng quan
Tài liệu này giải thích **CỰC KỲ CHI TIẾT** cách Command Side hoạt động trong CQRS - từng dòng code một!

---

## 🎯 VÍ DỤ THỰC TẾ - TẠO MỚI SÁCH

### 📝 Request bạn gửi:
```http
POST http://localhost:9001/api/v1/books
Content-Type: application/json

{
  "name": "Java Book 1",
  "author": "nvminh162"
}
```

### ✅ Response nhận được:
```
200 OK
Body: 0a977fd5-b39e-4ed3-b833-8fedc698e936
```

---

## 🚀 LUỒNG XỬ LÝ - TỪNG BƯỚC CHI TIẾT

---

## 📍 BƯỚC 1: CONTROLLER NHẬN REQUEST

### 📄 File: `BookCommandController.java`

```java
package com.nvminh162.bookservice.command.controller;

import com.nvminh162.bookservice.command.command.CreateBookCommand;
import com.nvminh162.bookservice.command.model.BookRequestModel;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController                          // ← Spring tạo REST API Controller
@RequestMapping("/api/v1/books")         // ← Base URL: /api/v1/books
@RequiredArgsConstructor                 // ← Lombok tự động tạo constructor với final fields
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)  // ← Tất cả field là private final
public class BookCommandController {

    CommandGateway commandGateway;       // ← Axon Framework's Command Bus (tự động inject)
    
    @PostMapping                         // ← Endpoint: POST /api/v1/books
    public String addBook(@RequestBody BookRequestModel model) {
        // DÒNG 1: Tạo Command object
        CreateBookCommand command = CreateBookCommand.builder()
                .id(UUID.randomUUID().toString())  
                .name(model.getName())              
                .author(model.getAuthor())          
                .isReady(true)
                .build();
        
        // DÒNG 2: Gửi command và đợi kết quả
        return commandGateway.sendAndWait(command);
    }
}
```

### 🔍 GIẢI THÍCH TỪNG DÒNG CODE:

#### **Khi request đến:**

**1️⃣ Spring nhận request:**
```java
POST /api/v1/books
Body: {"name": "Java Book 1", "author": "nvminh162"}
```
- Spring tự động **deserialize JSON** → `BookRequestModel` object
- `model.getName()` = "Java Book 1"
- `model.getAuthor()` = "nvminh162"

**2️⃣ Tạo Command object:**
```java
CreateBookCommand command = CreateBookCommand.builder()
    .id(UUID.randomUUID().toString())  // Tạo ID ngẫu nhiên: "0a977fd5-b39e-4ed3..."
    .name(model.getName())              // "Java Book 1"
    .author(model.getAuthor())          // "nvminh162"
    .isReady(true)                      // Mặc định sách sẵn sàng cho mượn
    .build();
```

**💡 Tại sao cần CreateBookCommand?**
- **Command** = Lệnh yêu cầu thay đổi dữ liệu
- Chứa đầy đủ thông tin cần thiết để tạo sách
- Immutable (không thay đổi được sau khi tạo)
- Có thể log, audit, replay sau này

**3️⃣ Gửi command qua CommandGateway:**
```java
return commandGateway.sendAndWait(command);
```

**❓ CommandGateway là gì?**
- Là **cổng giao tiếp** với Axon Framework
- **Không trực tiếp gọi** Aggregate, mà gửi command vào **Command Bus**
- Command Bus sẽ tìm đúng handler để xử lý

**❓ sendAndWait() làm gì?**
- **send**: Gửi command vào Command Bus
- **andWait**: Đợi cho đến khi xử lý xong (synchronous)
- Trả về kết quả: ID của Aggregate (Book ID)

---

## 📍 BƯỚC 2: AXON FRAMEWORK ROUTING COMMAND

### 🎯 Quá trình tự động của Axon:

```
CommandGateway.sendAndWait(command)
        ↓
  Command Bus (Axon internal)
        ↓
  Tìm Aggregate có @CommandHandler 
  phù hợp với CreateBookCommand
        ↓
  Gọi BookAggregate constructor
```

**💡 Bạn không thấy code này vì Axon Framework tự động xử lý!**

---

## 📍 BƯỚC 3: AGGREGATE XỬ LÝ COMMAND

### 📄 File: `BookAggregate.java`

```java
package com.nvminh162.bookservice.command.aggregate;

import com.nvminh162.bookservice.command.command.CreateBookCommand;
import com.nvminh162.bookservice.command.event.BookCreatedEvent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor                       // ← Axon cần default constructor
@Aggregate                               // ← Đánh dấu đây là Aggregate Root (Domain Model)
public class BookAggregate {

    @AggregateIdentifier                 // ← Định danh duy nhất của Aggregate
    String id;
    String name;
    String author;
    Boolean isReady;

    // ==================== COMMAND HANDLER ====================
    
    @CommandHandler                      // ← Axon gọi method này khi nhận CreateBookCommand
    public BookAggregate(CreateBookCommand command) {
        
        // **BƯỚC 3.1: Validate business rules (nếu cần)**
        // if (command.getName() == null || command.getName().isEmpty()) {
        //     throw new IllegalArgumentException("Book name is required");
        // }
        
        // **BƯỚC 3.2: Tạo Event**
        BookCreatedEvent event = new BookCreatedEvent();
        BeanUtils.copyProperties(command, event);
        // event.id = "0a977fd5-b39e-4ed3..."
        // event.name = "Java Book 1"
        // event.author = "nvminh162"
        // event.isReady = true
        
        // **BƯỚC 3.3: Apply Event - PHÁT TÁN SỰ KIỆN**
        AggregateLifecycle.apply(event);
        // Dòng này GỌI method on(BookCreatedEvent) bên dưới
        // và PHÁT event ra Event Bus
    }

    // ==================== EVENT SOURCING HANDLER ====================
    
    @EventSourcingHandler                // ← Tự động gọi khi event được apply
    public void on(BookCreatedEvent event) {
        // **BƯỚC 3.4: Cập nhật state của Aggregate**
        this.id = event.getId();         // "0a977fd5-b39e-4ed3..."
        this.name = event.getName();     // "Java Book 1"
        this.author = event.getAuthor(); // "nvminh162"
        this.isReady = event.getIsReady(); // true
        
        // ❗ State này chỉ tồn tại trong memory (RAM)
        // ❗ CHƯA LƯU VÀO DATABASE!
    }
}
```

### 🔍 GIẢI THÍCH TỪNG DÒNG CODE:

#### **DÒNG 1-2: Validate business rules (nếu có)**
```java
// if (command.getName() == null || command.getName().isEmpty()) {
//     throw new IllegalArgumentException("Book name is required");
// }
```
- Đây là nơi kiểm tra **business logic**
- VD: Tên sách không được rỗng, giá sách > 0, tồn kho >= 0...
- Nếu fail → throw Exception → Command bị reject → Client nhận lỗi 400/500

#### **DÒNG 3-4: Tạo Event**
```java
BookCreatedEvent event = new BookCreatedEvent();
BeanUtils.copyProperties(command, event);
```

**❓ Tại sao không lưu trực tiếp vào DB?**
- **Event Sourcing Pattern**: Lưu lại **lịch sử thay đổi** thay vì chỉ lưu state hiện tại
- Event = Sự kiện đã xảy ra trong quá khứ (BookCreatedEvent = Sách đã được tạo)
- Có thể **replay events** để rebuild lại state
- Có thể **audit**: Ai tạo? Khi nào? Dữ liệu gì?

**📦 BookCreatedEvent là gì?**
```java
public class BookCreatedEvent {
    String id;          // "0a977fd5-b39e-4ed3..."
    String name;        // "Java Book 1"
    String author;      // "nvminh162"
    Boolean isReady;    // true
}
```
- Là **POJO** chứa dữ liệu sự kiện
- Immutable (không thay đổi sau khi tạo)
- Được **broadcast** ra Event Bus

#### **DÒNG 5: Apply Event - CỐT LÕI CỦA CQRS!**
```java
AggregateLifecycle.apply(event);
```

**❓ Dòng này làm 3 việc quan trọng:**

**1. Gọi @EventSourcingHandler ngay lập tức:**
```java
@EventSourcingHandler
public void on(BookCreatedEvent event) {
    this.id = event.getId();       // Cập nhật state của Aggregate
    this.name = event.getName();
    this.author = event.getAuthor();
    this.isReady = event.getIsReady();
}
```
- Cập nhật **state trong memory** của Aggregate này
- Aggregate bây giờ biết: "Tôi là sách có ID xxx, tên xxx, tác giả xxx"

**2. Lưu Event vào Event Store:**
```
Axon tự động lưu event vào bảng:
DOMAIN_EVENT_ENTRY
- aggregate_identifier: "0a977fd5-b39e-4ed3..."
- type: "BookCreatedEvent"
- payload: {"id":"0a977fd5...", "name":"Java Book 1", ...}
- timestamp: "2026-02-13 10:30:00"
```
- Đây là **audit trail** - lịch sử đầy đủ
- Có thể replay để rebuild state

**3. Publish Event ra Event Bus:**
```
Event Bus (như một cái loa phóng thanh):
"Có sự kiện mới: BookCreatedEvent!"
```
- Tất cả **@EventHandler** đang lắng nghe sẽ nhận được event này
- Xử lý **bất đồng bộ** (async)

---

## 📍 BƯỚC 4: EVENT BUS BROADCAST EVENT

### 🔊 Axon Event Bus tự động phát tán:

```
Event Bus
    ↓
    ├─→ BookAggregate.on(BookCreatedEvent)         ✅ Đã chạy (ở Bước 3)
    ├─→ BookEventsHandler.on(BookCreatedEvent)     ← Sẽ chạy bây giờ!
    ├─→ BookProjection.on(BookCreatedEvent)        (Nếu có Query Side)
    └─→ NotificationService.on(BookCreatedEvent)   (Nếu có Service khác)
```

**💡 Tất cả @EventHandler sẽ nhận được event này!**

---

## 📍 BƯỚC 5: EVENT HANDLER LƯU VÀO DATABASE

### 📄 File: `BookEventsHandler.java`

```java
package com.nvminh162.bookservice.command.event;

import com.nvminh162.bookservice.command.data.Book;
import com.nvminh162.bookservice.command.data.BookRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component                               // ← Spring Bean (tự động tạo khi app start)
public class BookEventsHandler {

    BookRepository bookRepository;       // ← JPA Repository (tự động inject)

    @EventHandler                        // ← Lắng nghe tất cả BookCreatedEvent từ Event Bus
    public void on(BookCreatedEvent event) {
        
        // **BƯỚC 5.1: Tạo Entity từ Event**
        Book book = new Book();
        BeanUtils.copyProperties(event, book);
        // book.id = "0a977fd5-b39e-4ed3..."
        // book.name = "Java Book 1"
        // book.author = "nvminh162"
        // book.isReady = true
        
        // **BƯỚC 5.2: LƯU VÀO DATABASE - DÒNG QUAN TRỌNG NHẤT!**
        bookRepository.save(book);
        // JPA thực thi: INSERT INTO books VALUES (...)
    }
}
```

### 🔍 GIẢI THÍCH TỪNG DÒNG CODE:

#### **@EventHandler - Lắng nghe Event**
```java
@EventHandler
public void on(BookCreatedEvent event) { ... }
```

**❓ Khi nào method này được gọi?**
- Khi `AggregateLifecycle.apply(event)` chạy ở Aggregate
- Axon tự động gọi **TẤT CẢ** @EventHandler có tham số `BookCreatedEvent`
- Chạy **bất đồng bộ** (có thể delay vài milliseconds)

**❓ Tại sao lại lưu DB ở đây, không lưu ở Aggregate?**
- **Separation of Concerns**: 
  - Aggregate = Business logic
  - EventHandler = Side effects (lưu DB, gửi email, log...)
- **Event-driven**: Nhiều handler có thể xử lý cùng 1 event
- **Decoupling**: Aggregate không phụ thuộc vào DB implementation

#### **bookRepository.save(book) - Lưu vào DB**
```java
bookRepository.save(book);
```

**❓ Điều gì xảy ra trong DB?**
```sql
INSERT INTO books (id, name, author, is_ready) 
VALUES ('0a977fd5-b39e-4ed3-b833-8fedc698e936', 'Java Book 1', 'nvminh162', true);
```

**📊 Bảng `books` bây giờ có data:**
| id | name | author | is_ready |
|----|------|--------|----------|
| 0a977fd5-b39e-4ed3... | Java Book 1 | nvminh162 | true |

---

## 📍 BƯỚC 6: TRẢ KẾT QUẢ VỀ CLIENT

### 🔙 Quay lại Controller:

```java
@PostMapping
public String addBook(@RequestBody BookRequestModel model) {
    CreateBookCommand command = CreateBookCommand.builder()...build();
    
    // Dòng này ĐỢI cho đến khi:
    // 1. Aggregate xử lý xong Command
    // 2. Event được apply
    // 3. Event Store lưu xong
    // 4. Trả về Aggregate ID
    return commandGateway.sendAndWait(command);
    
    // Response: "0a977fd5-b39e-4ed3-b833-8fedc698e936"
}
```

**❓ Client nhận được gì?**
```http
HTTP/1.1 200 OK
Content-Type: text/plain

0a977fd5-b39e-4ed3-b833-8fedc698e936
```

**❗ Lưu ý:** EventHandler có thể vẫn đang chạy (async), nhưng Controller đã trả response!

---

## 📊 SƠ ĐỒ TỔNG QUAN LUỒNG COMMAND

```
┌─────────────────────────────────────────────────────────────────┐
│                     CLIENT GỬI REQUEST                           │
│  POST /api/v1/books                                             │
│  Body: {"name": "Java Book 1", "author": "nvminh162"}           │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ↓
┌─────────────────────────────────────────────────────────────────┐
│  📍 BƯỚC 1: BookCommandController                               │
│  ─────────────────────────────────────────────────────────────  │
│  @PostMapping                                                   │
│  public String addBook(@RequestBody BookRequestModel model) {   │
│      CreateBookCommand command = CreateBookCommand.builder()    │
│          .id(UUID.randomUUID().toString())  // Tạo ID          │
│          .name(model.getName())                                 │
│          .author(model.getAuthor())                             │
│          .isReady(true)                                         │
│          .build();                                              │
│      return commandGateway.sendAndWait(command); // Gửi command │
│  }                                                              │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ↓
┌─────────────────────────────────────────────────────────────────┐
│  📍 BƯỚC 2: Axon Command Bus (Tự động)                          │
│  ─────────────────────────────────────────────────────────────  │
│  - Nhận CreateBookCommand từ CommandGateway                     │
│  - Tìm Aggregate có @CommandHandler phù hợp                     │
│  - Gọi BookAggregate constructor                                │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ↓
┌─────────────────────────────────────────────────────────────────┐
│  📍 BƯỚC 3: BookAggregate                                       │
│  ─────────────────────────────────────────────────────────────  │
│  @CommandHandler                                                │
│  public BookAggregate(CreateBookCommand command) {              │
│                                                                 │
│      // 3.1: Validate business rules                            │
│      // if (invalid) throw Exception;                           │
│                                                                 │
│      // 3.2: Tạo Event                                          │
│      BookCreatedEvent event = new BookCreatedEvent();           │
│      BeanUtils.copyProperties(command, event);                  │
│                                                                 │
│      // 3.3: Apply Event (CỐT LÕI!)                             │
│      AggregateLifecycle.apply(event);                           │
│      // → Gọi @EventSourcingHandler                             │
│      // → Lưu vào Event Store                                   │
│      // → Publish ra Event Bus                                  │
│  }                                                              │
│                                                                 │
│  @EventSourcingHandler                                          │
│  public void on(BookCreatedEvent event) {                       │
│      // 3.4: Cập nhật state trong memory                        │
│      this.id = event.getId();                                   │
│      this.name = event.getName();                               │
│      this.author = event.getAuthor();                           │
│      this.isReady = event.getIsReady();                         │
│  }                                                              │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ↓
┌─────────────────────────────────────────────────────────────────┐
│  📍 BƯỚC 4: Event Bus Broadcast                                 │
│  ─────────────────────────────────────────────────────────────  │
│  Event Bus phát tán BookCreatedEvent đến:                       │
│  ├─→ BookEventsHandler.on(BookCreatedEvent)   ← Lưu DB        │
│  ├─→ BookProjection.on(BookCreatedEvent)      (nếu có Query)  │
│  └─→ Các service khác...                                        │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ↓
┌─────────────────────────────────────────────────────────────────┐
│  📍 BƯỚC 5: BookEventsHandler                                   │
│  ─────────────────────────────────────────────────────────────  │
│  @EventHandler                                                  │
│  public void on(BookCreatedEvent event) {                       │
│      // 5.1: Tạo Entity từ Event                                │
│      Book book = new Book();                                    │
│      BeanUtils.copyProperties(event, book);                     │
│                                                                 │
│      // 5.2: LƯU VÀO DATABASE!                                  │
│      bookRepository.save(book);                                 │
│      // → JPA: INSERT INTO books VALUES (...)                   │
│  }                                                              │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ↓
┌─────────────────────────────────────────────────────────────────┐
│  💾 DATABASE (H2)                                               │
│  ─────────────────────────────────────────────────────────────  │
│  Table: books                                                   │
│  ┌────────────────────┬──────────────┬───────────┬──────────┐  │
│  │ id                 │ name         │ author    │ is_ready │  │
│  ├────────────────────┼──────────────┼───────────┼──────────┤  │
│  │ 0a977fd5-b39e-4ed3 │ Java Book 1  │ nvminh162 │ true     │  │
│  └────────────────────┴──────────────┴───────────┴──────────┘  │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ↓
┌─────────────────────────────────────────────────────────────────┐
│  📍 BƯỚC 6: Trả response về Client                              │
│  ─────────────────────────────────────────────────────────────  │
│  return commandGateway.sendAndWait(command);                    │
│  // Trả về: "0a977fd5-b39e-4ed3-b833-8fedc698e936"             │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ↓
┌─────────────────────────────────────────────────────────────────┐
│                     CLIENT NHẬN RESPONSE                         │
│  HTTP 200 OK                                                    │
│  Body: "0a977fd5-b39e-4ed3-b833-8fedc698e936"                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🎯 TIMELINE - THỜI GIAN THỰC

```
t=0ms    │ Client gửi POST request
         ↓
t=5ms    │ Controller nhận request
         │ → Tạo CreateBookCommand
         │ → Gọi commandGateway.sendAndWait(command)
         ↓
t=10ms   │ Axon Command Bus routing
         │ → Tìm BookAggregate
         │ → Gọi @CommandHandler
         ↓
t=15ms   │ BookAggregate.@CommandHandler chạy
         │ → Validate (nếu có)
         │ → Tạo BookCreatedEvent
         │ → AggregateLifecycle.apply(event)
         │   ├─→ Gọi @EventSourcingHandler (cập nhật state)
         │   ├─→ Lưu Event vào Event Store
         │   └─→ Publish event ra Event Bus
         ↓
t=20ms   │ sendAndWait() trả về Aggregate ID
         │ Controller return response
         ↓
t=25ms   │ Client nhận response 200 OK ✅
         │
         ↓
t=30ms   │ EventHandler chạy bất đồng bộ
         │ → BookEventsHandler.on(BookCreatedEvent)
         │ → bookRepository.save(book)
         │ → INSERT INTO books...
         ↓
t=40ms   │ ✅ Data đã được lưu vào DB!
```

**💡 Chú ý:** Client nhận response (t=25ms) TRƯỚC KHI data lưu vào DB (t=40ms)!

---

## ❓ CÂU HỎI THƯỜNG GẶP

### 1. **Tại sao không lưu trực tiếp vào DB trong Aggregate?**

**❌ Cách truyền thống:**
```java
@CommandHandler
public BookAggregate(CreateBookCommand command) {
    bookRepository.save(book);  // ← Lưu trực tiếp
}
```

**✅ Cách CQRS + Event Sourcing:**
```java
@CommandHandler
public BookAggregate(CreateBookCommand command) {
    AggregateLifecycle.apply(event);  // ← Phát event
}

@EventHandler  // ← Lưu DB ở đây
public void on(BookCreatedEvent event) {
    bookRepository.save(book);
}
```

**Lý do:**
- **Separation of Concerns**: Aggregate chỉ lo business logic, không lo DB
- **Event Sourcing**: Lưu lịch sử thay đổi (audit trail)
- **Extensibility**: Thêm handler mới không cần sửa Aggregate
- **Testability**: Aggregate dễ test hơn (không phụ thuộc DB)

---

### 2. **EventHandler có thể fail không? Nếu fail thì sao?**

**Có thể fail!** VD: Database bị down, network timeout...

**Giải pháp:**
- Axon có **Tracking Event Processor** - tự động retry
- Event được lưu trong Event Store → có thể replay
- Có thể config Dead Letter Queue cho failed events

```java
@EventHandler
public void on(BookCreatedEvent event) {
    try {
        bookRepository.save(book);
    } catch (Exception e) {
        // Log error, retry sau, hoặc gửi alert
        throw e;  // Axon sẽ retry
    }
}
```

---

### 3. **Aggregate state lưu ở đâu?**

**2 nơi:**

**1️⃣ Event Store (Axon tự động):**
```
Table: DOMAIN_EVENT_ENTRY
- aggregate_identifier: "0a977fd5-b39e-4ed3..."
- sequence_number: 0, 1, 2, ... (thứ tự events)
- type: "BookCreatedEvent", "BookUpdatedEvent", ...
- payload: JSON của event
```

**2️⃣ Application Database (bạn tự lưu):**
```
Table: books
- id, name, author, is_ready
```

**💡 Có thể rebuild state từ Event Store:**
```java
// Replay tất cả events của aggregate này
List<Event> events = eventStore.readEvents("0a977fd5...");
BookAggregate aggregate = new BookAggregate();
events.forEach(event -> aggregate.on(event));
// aggregate bây giờ có state giống như khi nó được tạo!
```

---

### 4. **Tại sao phải dùng @TargetAggregateIdentifier?**

```java
public class CreateBookCommand {
    @TargetAggregateIdentifier  // ← Annotation này
    String id;
    // ...
}
```

**Lý do:**
- Axon cần biết command này thuộc về **Aggregate nào**
- Khi update/delete: `UpdateBookCommand(id="abc")` → Axon load `BookAggregate("abc")`
- Khi create: ID chưa tồn tại → Axon tạo Aggregate mới

---

### 5. **sendAndWait() vs send() khác nhau gì?**

**sendAndWait():**
```java
String bookId = commandGateway.sendAndWait(command);
// ĐỢI cho đến khi xử lý xong, rồi mới chạy tiếp
// Trả về kết quả: Aggregate ID
```

**send():**
```java
CompletableFuture<String> future = commandGateway.send(command);
// KHÔNG đợi, chạy async
// Trả về Future, phải gọi .get() hoặc .join() để lấy kết quả
```

**💡 Dùng sendAndWait() cho REST API đồng bộ (như ví dụ của bạn)**

---

## 🎓 TÓM TẮT LUỒNG COMMAND

### **6 bước xử lý:**

1. **Controller** nhận request → Tạo Command → Gửi qua CommandGateway
2. **Axon Command Bus** routing command đến đúng Aggregate
3. **Aggregate @CommandHandler** validate → Tạo Event → Apply Event
4. **Event Bus** broadcast event đến tất cả EventHandler
5. **EventHandler** lắng nghe event → Lưu vào Database
6. **Controller** nhận Aggregate ID → Trả về Client

### **Công thức:**

```
Request → Command → Aggregate → Event → EventHandler → Database
                                  ↓
                             Event Bus
```

### **Từ khóa quan trọng:**

- **Command** = Lệnh yêu cầu thay đổi (CreateBookCommand)
- **Aggregate** = Domain model chứa business logic (BookAggregate)
- **Event** = Sự kiện đã xảy ra (BookCreatedEvent)
- **EventHandler** = Xử lý side effects (BookEventsHandler)
- **CommandGateway** = Cổng gửi command
- **Event Bus** = Cơ chế phát tán event

---

## 🚀 KẾT LUẬN

**Command Side** trong CQRS là luồng xử lý **thay đổi dữ liệu** theo pattern Event Sourcing:

✅ **Không lưu trực tiếp DB** → Tạo Event → Event làm nguồn tin cậy  
✅ **Aggregate** chứa business logic, không biết về DB  
✅ **EventHandler** lo việc lưu DB và side effects  
✅ **Event-driven** → Decoupling, dễ mở rộng  
✅ **Audit trail** → Lưu lại toàn bộ lịch sử thay đổi  

**Điều quan trọng nhất:** `AggregateLifecycle.apply(event)` là trái tim của CQRS - nó kết nối tất cả lại với nhau!


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

