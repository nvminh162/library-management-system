# MapStruct - Hướng dẫn đầy đủ

## Mục lục

1. [MapStruct là gì?](#1-mapstruct-là-gì)
2. [Cài đặt](#2-cài-đặt)
3. [Các trường hợp phổ biến](#3-các-trường-hợp-phổ-biến)
   - [Case 1: Map cơ bản (cùng tên field)](#case-1-map-cơ-bản-cùng-tên-field)
   - [Case 2: Đổi tên field (source → target)](#case-2-đổi-tên-field-source--target)
   - [Case 3: Ignore field](#case-3-ignore-field)
   - [Case 4: Nested Object (obj.field)](#case-4-nested-object-objfield)
   - [Case 5: Custom expression java(...)](#case-5-custom-expression-java)
   - [Case 6: Update với @MappingTarget](#case-6-update-với-mappingtarget)
   - [Case 7: @BeanMapping - Partial Update PATCH](#case-7-beanmapping---partial-update-patch)
   - [Case 8: default method - Custom Logic phức tạp](#case-8-default-method---custom-logic-phức-tạp)
   - [Case 9: Map List/Set tự động](#case-9-map-listset-tự-động)
   - [Case 10: Dùng nhiều Mapper (uses)](#case-10-dùng-nhiều-mapper-uses)
   - [Case 11: @AfterMapping / @BeforeMapping](#case-11-aftermapping--beforemapping)
   - [Case 12: Constant và Default Value](#case-12-constant-và-default-value)
4. [Ví dụ thực tế: UserController](#4-ví-dụ-thực-tế-usercontroller)
5. [Bảng tra cứu nhanh](#5-bảng-tra-cứu-nhanh)

---

## 1. MapStruct là gì?

**MapStruct** là thư viện Java dùng để **tự động generate code mapping** giữa các object (DTO ↔ Entity) tại **compile time**.

```
Client JSON  →  DTO (Request)  →  Entity  →  DB
                                     ↓
Client JSON  ←  DTO (Response) ←  Entity  ←  DB
```

**Ưu điểm so với ModelMapper:**
- Generate code tại compile time → **nhanh hơn** (không dùng reflection)
- **Type-safe** → lỗi phát hiện ngay lúc build
- Dễ debug (xem file `*MapperImpl.java` được generate)

---

## 2. Cài đặt

```xml
<!-- pom.xml -->
<properties>
    <org.mapstruct.version>1.5.5.Final</org.mapstruct.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>${org.mapstruct.version}</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessorPaths>
                    <!-- Nếu dùng Lombok thì phải để Lombok TRƯỚC MapStruct -->
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>${lombok.version}</version>
                    </path>
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>${org.mapstruct.version}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

## 3. Các trường hợp phổ biến

---

### Case 1: Map cơ bản (cùng tên field)

#### 📋 Đề bài
Có `User` entity và `UserResponse` DTO. Tất cả field **cùng tên, cùng kiểu dữ liệu**.
Yêu cầu: Map `User` → `UserResponse` để trả về cho client.

```
User (Entity)            UserResponse (DTO)
─────────────────        ──────────────────
String id          →     String id
String username    →     String username
String email       →     String email
```

#### ✅ Mapper

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    // Không cần @Mapping gì cả — MapStruct tự map theo tên field
    UserResponse toUserResponse(User user);
}
```

#### 🔍 Code MapStruct generate ra (để hiểu bên dưới)

```java
@Component
public class UserMapperImpl implements UserMapper {
    @Override
    public UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());             // id → id
        response.setUsername(user.getUsername()); // username → username
        response.setEmail(user.getEmail());       // email → email
        return response;
    }
}
```

#### 📤 Kết quả

```
Input  (User entity từ DB):   { id: "abc123", username: "minh", email: "minh@mail.com" }
Output (UserResponse trả về): { id: "abc123", username: "minh", email: "minh@mail.com" }
```

---

### Case 2: Đổi tên field (source → target)

#### 📋 Đề bài
Client gửi JSON có field `userName` và `userEmail`,
nhưng trong Entity tên field là `username` và `email`.
Yêu cầu: Map `UserCreationRequest` → `User` với tên field khác nhau.

```
UserCreationRequest (DTO)     User (Entity)
─────────────────────────     ─────────────
String userName         →     String username   ← tên khác!
String userEmail        →     String email      ← tên khác!
String password         →     String password   ← cùng tên, tự map
String firstName        →     String firstName  ← cùng tên, tự map
```

#### ✅ Mapper

```java
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "userName",  target = "username") // DTO.userName → Entity.username
    @Mapping(source = "userEmail", target = "email")    // DTO.userEmail → Entity.email
    // password, firstName tự map vì cùng tên
    User toUser(UserCreationRequest request);
}
```

#### 📤 Kết quả

```
Input (JSON từ client):
{
    "userName":  "nvminh162",
    "userEmail": "minh@mail.com",
    "password":  "123456",
    "firstName": "Minh"
}

Output (User entity):
user.username  = "nvminh162"     ← từ userName
user.email     = "minh@mail.com" ← từ userEmail
user.password  = "123456"        ← tự map (cùng tên)
user.firstName = "Minh"          ← tự map (cùng tên)
```

---

### Case 3: Ignore field

#### 📋 Đề bài
Khi tạo User mới, có một số field **không nên map** từ DTO vào Entity vì:
- `id` → DB tự generate (UUID)
- `createdAt` → DB tự set timestamp
- `password` → phải encode bằng BCrypt trong Service, không map thẳng
- `roles` → set mặc định trong Service, không để client tự truyền

Yêu cầu: Bỏ qua các field đó khi mapping.

#### ✅ Mapper

```java
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id",        ignore = true) // DB tự generate → không map
    @Mapping(target = "createdAt", ignore = true) // DB tự set → không map
    @Mapping(target = "password",  ignore = true) // encode riêng → không map
    @Mapping(target = "roles",     ignore = true) // set trong Service → không map
    @Mapping(source = "userName",  target = "username")
    @Mapping(source = "userEmail", target = "email")
    User toUser(UserCreationRequest request);
}
```

#### 🔧 Trong Service — set thủ công các field bị ignore

```java
User user = userMapper.toUser(request);

// Tự set các field bị ignore
user.setPassword(passwordEncoder.encode(request.getPassword())); // encode BCrypt
user.setRoles(Set.of("USER"));                                   // role mặc định
user.setCreatedAt(LocalDateTime.now());                          // timestamp hiện tại
// id sẽ do DB tự generate khi save()

userRepository.save(user);
```

#### 📤 Kết quả

```
Input (JSON từ client):
{ "userName": "nvminh162", "userEmail": "minh@mail.com", "password": "123456" }

Sau mapper.toUser():
user.id        = null           ← ignore → DB generate lúc save()
user.username  = "nvminh162"
user.email     = "minh@mail.com"
user.password  = null           ← ignore → Service sẽ encode rồi set
user.roles     = null           ← ignore → Service sẽ set ["USER"]
user.createdAt = null           ← ignore → Service sẽ set now()

Sau Service xử lý + save vào DB:
user.id        = "uuid-abc-123"
user.username  = "nvminh162"
user.email     = "minh@mail.com"
user.password  = "$2a$10$..."   ← đã encode BCrypt
user.roles     = ["USER"]
user.createdAt = 2026-02-23T10:00:00
```

---

### Case 4: Nested Object (obj.field)

#### 📋 Đề bài
Entity `Order` chứa object `Customer` lồng bên trong.
Response DTO muốn **làm phẳng (flatten)** — lấy thẳng các field của `Customer` ra ngoài.

```
Order  (Entity)                  OrderResponse (DTO)
──────────────────────           ────────────────────────
Customer customer {         →    String customerId      ← customer.id
    String id               →    String customerName    ← customer.fullName
    String fullName         →    String customerPhone   ← customer.phone
    String phone            }
String totalAmount          →    String totalAmount     ← cùng tên, tự map
```

#### ✅ Mapper

```java
@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "customer.id",       target = "customerId")
    @Mapping(source = "customer.fullName", target = "customerName")
    @Mapping(source = "customer.phone",    target = "customerPhone")
    // totalAmount tự map vì cùng tên
    OrderResponse toOrderResponse(Order order);
}
```

#### 📤 Kết quả

```
Input (Order entity từ DB):
order.customer.id       = "cust-001"
order.customer.fullName = "Nguyen Van Minh"
order.customer.phone    = "0901234567"
order.totalAmount       = "500000"

Output (OrderResponse trả về cho client):
{
    "customerId":    "cust-001",
    "customerName":  "Nguyen Van Minh",
    "customerPhone": "0901234567",
    "totalAmount":   "500000"
}
```

---

### Case 5: Custom expression java(...)

#### 📋 Đề bài
Response DTO cần các field được **tính toán / xử lý** từ Entity, không chỉ copy thẳng:
- `fullName` = `firstName` + `" "` + `lastName`
- `roleIds` = lấy chỉ `id` từ `Set<Role>` → thành `Set<String>`
- `createdDate` = format `LocalDateTime` → `String "dd/MM/yyyy"`

#### ✅ Mapper

```java
@Mapper(componentModel = "spring", imports = {Collectors.class, DateTimeFormatter.class})
public interface UserMapper {

    @Mapping(
        target     = "fullName",
        expression = "java(user.getFirstName() + \" \" + user.getLastName())"
    )
    @Mapping(
        target     = "roleIds",
        expression = "java(user.getRoles().stream().map(r -> r.getId()).collect(Collectors.toSet()))"
    )
    @Mapping(
        target     = "createdDate",
        expression = "java(user.getCreatedAt().format(DateTimeFormatter.ofPattern(\"dd/MM/yyyy\")))"
    )
    UserResponse toUserResponse(User user);
}
```

> **Lưu ý:** Khai báo `imports` trong `@Mapper` để dùng tên class ngắn gọn.
> Nếu không khai báo, phải viết full package trong expression:
> `java.util.stream.Collectors.toSet()`, `java.time.format.DateTimeFormatter.ofPattern(...)`

#### 📤 Kết quả

```
Input (User entity từ DB):
user.firstName = "Nguyen Van"
user.lastName  = "Minh"
user.roles     = [Role{id:"r1"}, Role{id:"r2"}]
user.createdAt = 2026-01-15T08:30:00

Output (UserResponse trả về cho client):
{
    "fullName":    "Nguyen Van Minh",
    "roleIds":     ["r1", "r2"],
    "createdDate": "15/01/2026"
}
```

---

### Case 6: Update với @MappingTarget

#### 📋 Đề bài
Client gửi request **cập nhật thông tin User**.
Thay vì tạo `User` mới, cần **cập nhật thẳng vào entity đã lấy từ DB**
để giữ nguyên `id`, `createdAt`, và các field không được update.

#### ✅ Mapper

```java
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id",        ignore = true) // giữ nguyên id từ DB
    @Mapping(target = "roles",     ignore = true) // không cho đổi roles
    @Mapping(target = "password",  ignore = true) // đổi password riêng
    @Mapping(target = "createdAt", ignore = true) // giữ nguyên createdAt
    @Mapping(source = "userEmail", target = "email")
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
    //              ↑ Entity lấy từ DB — UPDATE trực tiếp, không tạo object mới
}
```

#### 🔧 Trong Service

```java
// 1. Lấy entity đang có trong DB
User user = userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("User not found"));

// 2. Map request vào entity đang có (UPDATE trực tiếp)
userMapper.updateUser(user, request);

// 3. Lưu lại
userRepository.save(user);
```

#### 📤 Kết quả

```
User trong DB trước khi update:
user.id        = "abc123"
user.username  = "nvminh162"
user.email     = "old@mail.com"
user.roles     = ["ADMIN"]       ← ignore → KHÔNG bị đổi
user.createdAt = 2026-01-01

Request từ client:
{ "firstName": "Minh", "userEmail": "new@mail.com" }

User sau updateUser() + save():
user.id        = "abc123"          ← giữ nguyên (ignore)
user.username  = "nvminh162"       ← giữ nguyên (không có trong request)
user.email     = "new@mail.com"    ← đã update
user.firstName = "Minh"            ← đã update
user.roles     = ["ADMIN"]         ← giữ nguyên (ignore)
user.createdAt = 2026-01-01        ← giữ nguyên (ignore)
```

---

### Case 7: @BeanMapping - Partial Update PATCH

#### 📋 Đề bài
Client muốn **chỉ cập nhật firstName**, các field còn lại không gửi (null).
Nếu không có `@BeanMapping`, MapStruct sẽ **ghi đè null** vào DB → mất data.

Yêu cầu: Field nào client **không gửi (null)** thì giữ nguyên giá trị cũ trong DB.

#### ❌ Không có @BeanMapping — NGUY HIỂM

```java
void updateUser(@MappingTarget User user, UserUpdateRequest request);
```

```
Client gửi: { "firstName": "Minh" }   (lastName và email không gửi → null)

User trong DB:                   User sau update:
firstName = "Nguyen"  → "Minh"   firstName = "Minh"
lastName  = "Van A"   → null  ❌ lastName  = null     ← MẤT DATA!
email     = "a@x.com" → null  ❌ email     = null     ← MẤT DATA!
```

#### ✅ Có @BeanMapping IGNORE

```java
@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
void updateUser(@MappingTarget User user, UserUpdateRequest request);
```

#### 🔍 Code MapStruct generate ra

```java
@Override
public void updateUser(User user, UserUpdateRequest request) {
    if (request.getFirstName() != null) {    // có giá trị → set
        user.setFirstName(request.getFirstName());
    }
    if (request.getLastName() != null) {     // null → bỏ qua, GIỮ NGUYÊN
        user.setLastName(request.getLastName());
    }
    if (request.getUserEmail() != null) {    // null → bỏ qua, GIỮ NGUYÊN
        user.setEmail(request.getUserEmail());
    }
}
```

#### 📤 Kết quả

```
PATCH /users/abc123
Body: { "firstName": "Minh" }   (chỉ gửi firstName, lastName và email là null)

Trước:  { firstName: "Nguyen", lastName: "Van A", email: "a@x.com", roles: ["ADMIN"] }

Sau:    { firstName: "Minh",   lastName: "Van A", email: "a@x.com", roles: ["ADMIN"] }
                    ↑ đổi          ↑ giữ nguyên       ↑ giữ nguyên      ↑ giữ nguyên
```

---

### Case 8: default method - Custom Logic phức tạp

#### 📋 Đề bài
Entity `User` có `Set<Role>` (object đầy đủ).
Response DTO cần `Set<RoleInfo>` (chỉ lấy `id` và `name` của Role).
Logic quá dài để viết trong `expression` → cần viết thành method riêng.

```
User.roles = Set<Role>            UserResponse.roles = Set<RoleInfo>
─────────────────────             ────────────────────────────────
Role {                      →     RoleInfo {
    String id               →         String id
    String name             →         String name
    String description  ✗             (bỏ description)
    LocalDateTime createdAt ✗         (bỏ createdAt)
}                                 }
```

#### ✅ Mapper

```java
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    UserResponse toUserResponse(User user);

    // MapStruct tự nhận diện và gọi method này khi cần map Set<Role>
    default Set<UserResponse.RoleInfo> mapRoles(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) return null;

        return roles.stream()
                .map(role -> UserResponse.RoleInfo.builder()
                        .id(role.getId())
                        .name(role.getName())
                        // description và createdAt không lấy
                        .build())
                .collect(Collectors.toSet());
    }
}
```

#### 📤 Kết quả

```
Input (User entity từ DB):
user.roles = [
    Role{ id: "r1", name: "ADMIN", description: "Administrator", createdAt: 2026-01-01 },
    Role{ id: "r2", name: "USER",  description: "Normal User",   createdAt: 2026-01-01 }
]

Output (UserResponse trả về):
{
    "roles": [
        { "id": "r1", "name": "ADMIN" },
        { "id": "r2", "name": "USER"  }
    ]
}
← description và createdAt bị bỏ qua
```

---

### Case 9: Map List/Set tự động

#### 📋 Đề bài
Endpoint `GET /users` cần trả về **danh sách tất cả users**.
Đã có method map 1 `User` → `UserResponse`, cần map cho cả `List<User>`.

#### ✅ Mapper

```java
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toUserResponse(User user); // map 1 user

    // Chỉ cần khai báo — MapStruct tự generate dựa vào toUserResponse() ở trên
    List<UserResponse> toUserResponseList(List<User> users);
}
```

#### 🔍 Code MapStruct generate ra

```java
@Override
public List<UserResponse> toUserResponseList(List<User> users) {
    if (users == null) return null;
    return users.stream()
            .map(user -> toUserResponse(user)) // gọi lại method đã có
            .collect(Collectors.toList());
}
```

#### 📤 Kết quả

```
Input (List<User> từ DB):
[
    User{ id: "1", username: "minh",  email: "minh@mail.com"  },
    User{ id: "2", username: "admin", email: "admin@mail.com" }
]

Output (List<UserResponse> trả về):
[
    { "id": "1", "username": "minh",  "email": "minh@mail.com"  },
    { "id": "2", "username": "admin", "email": "admin@mail.com" }
]
```

---

### Case 10: Dùng nhiều Mapper (uses)

#### 📋 Đề bài
`UserMapper` cần map `Set<Role>` → `Set<RoleResponse>`.
Đã có sẵn `RoleMapper` biết cách map `Role` → `RoleResponse`.
Yêu cầu: **Tái sử dụng** `RoleMapper` thay vì viết lại logic.

#### ✅ Mapper

```java
// RoleMapper — map Role → RoleResponse
@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleResponse toRoleResponse(Role role);
}

// UserMapper — dùng lại RoleMapper
@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {
    // MapStruct thấy cần map Set<Role> → Set<RoleResponse>
    // → Tự động dùng RoleMapper.toRoleResponse() cho từng element
    UserResponse toUserResponse(User user);
}
```

#### 📤 Kết quả

```
Input (User entity):
user.roles = [Role{ id: "r1", name: "ADMIN" }, Role{ id: "r2", name: "USER" }]

Output (UserResponse):
{
    "roles": [
        { "id": "r1", "name": "ADMIN" },
        { "id": "r2", "name": "USER"  }
    ]
}
↑ MapStruct tự dùng RoleMapper để map từng Role → RoleResponse
  Không cần viết lại logic map trong UserMapper
```

---

### Case 11: @AfterMapping / @BeforeMapping

#### 📋 Đề bài
- **@AfterMapping:** Sau khi map xong `User` → `UserResponse`, cần set thêm `fullName` = `firstName + lastName`
- **@BeforeMapping:** Trước khi map request vào entity, cần chuẩn hóa `username` → trim khoảng trắng + lowercase

#### ✅ Mapper

```java
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toUserResponse(User user);
    User toUser(UserCreationRequest request);

    // Chạy SAU khi map User → UserResponse xong
    @AfterMapping
    default void setFullName(User user, @MappingTarget UserResponse response) {
        response.setFullName(user.getFirstName() + " " + user.getLastName());
    }

    // Chạy TRƯỚC khi map UserCreationRequest → User
    @BeforeMapping
    default void normalizeUsername(UserCreationRequest request) {
        if (request.getUserName() != null) {
            request.setUserName(request.getUserName().trim().toLowerCase());
        }
    }
}
```

#### 📤 Kết quả

```
── @BeforeMapping ──────────────────────────────────────────────────

Input (JSON từ client):    { "userName": "  NVMinh162  " }

Sau @BeforeMapping:        request.userName = "nvminh162"   ← trim + toLowerCase
Sau toUser():              user.username    = "nvminh162"


── @AfterMapping ───────────────────────────────────────────────────

Input (User entity):       { firstName: "Nguyen Van", lastName: "Minh" }

Sau toUserResponse():      response.firstName = "Nguyen Van"
                           response.lastName  = "Minh"
                           response.fullName  = null          ← chưa có

Sau @AfterMapping:         response.fullName  = "Nguyen Van Minh" ✅
```

---

### Case 12: Constant và Default Value

#### 📋 Đề bài
Khi tạo User mới, một số field cần có **giá trị cố định** hoặc **giá trị mặc định**:
- `status` luôn = `"ACTIVE"` cho user mới (không phụ thuộc Request)
- `point` = `0` nếu Request không gửi giá trị (`null`)
- `avatarUrl` = `"/default-avatar.png"` nếu không có ảnh

#### ✅ Mapper

```java
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "status",    constant = "ACTIVE")           // luôn = "ACTIVE"
    @Mapping(target = "type",      constant = "NORMAL")           // luôn = "NORMAL"
    @Mapping(target = "point",     defaultValue = "0")            // = 0 nếu null
    @Mapping(target = "avatarUrl", defaultValue = "/default.png") // = "/default.png" nếu null
    User toUser(UserCreationRequest request);
}
```

#### 📤 Kết quả

```
── constant: LUÔN set giá trị cố định, bất kể Request có gì ────────

Input:   { ... }           (không có field status, type)
Output:  user.status = "ACTIVE"   ← luôn luôn
         user.type   = "NORMAL"   ← luôn luôn


── defaultValue: chỉ set khi source là null ─────────────────────────

Input:   { "point": null, "avatarUrl": null }
Output:  user.point     = "0"              ← dùng default (null)
         user.avatarUrl = "/default.png"   ← dùng default (null)

Input:   { "point": "100", "avatarUrl": "/my-photo.png" }
Output:  user.point     = "100"            ← dùng giá trị từ Request
         user.avatarUrl = "/my-photo.png"  ← dùng giá trị từ Request
```

---

## 4. Ví dụ thực tế: UserController

### Cấu trúc class

```java
// ── Entity ──────────────────────────────────────────────
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String status;
    @ManyToMany
    private Set<Role> roles;
    private LocalDateTime createdAt;
}

// ── Request DTOs ─────────────────────────────────────────
@Data
public class UserCreationRequest {
    private String userName;   // tên khác: userName → username
    private String userEmail;  // tên khác: userEmail → email
    private String password;
    private String firstName;
    private String lastName;
}

@Data
public class UserUpdateRequest {
    private String firstName;  // có thể null → dùng @BeanMapping IGNORE
    private String lastName;
    private String userEmail;  // tên khác: userEmail → email
}

// ── Response DTO ─────────────────────────────────────────
@Data
@Builder
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private String fullName;   // = firstName + " " + lastName
    private String status;
    private Set<RoleInfo> roles;

    @Data
    @Builder
    public static class RoleInfo {
        private String id;
        private String name;
    }
}
```

### UserMapper — tổng hợp đầy đủ

```java
@Mapper(componentModel = "spring")
public interface UserMapper {

    // ── CREATE: Request → Entity ──────────────────────────
    @Mapping(target = "id",        ignore = true)       // DB tự generate
    @Mapping(target = "roles",     ignore = true)       // set trong Service
    @Mapping(target = "createdAt", ignore = true)       // DB tự set
    @Mapping(target = "password",  ignore = true)       // encode trong Service
    @Mapping(target = "status",    constant = "ACTIVE") // mặc định ACTIVE
    @Mapping(source = "userName",  target = "username") // đổi tên field
    @Mapping(source = "userEmail", target = "email")    // đổi tên field
    User toUser(UserCreationRequest request);

    // ── READ: Entity → Response ───────────────────────────
    @Mapping(source = "username", target = "username")
    @Mapping(source = "email",    target = "email")
    @Mapping(
        target     = "fullName",
        expression = "java(user.getFirstName() + \" \" + user.getLastName())"
    )
    @Mapping(
        target     = "roles",
        expression = "java(mapRoles(user.getRoles()))"
    )
    UserResponse toUserResponse(User user);

    // Map List tự động
    List<UserResponse> toUserResponseList(List<User> users);

    // ── UPDATE: PATCH (chỉ update field có giá trị) ───────
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "roles",     ignore = true)      // không cho đổi roles
    @Mapping(target = "password",  ignore = true)      // đổi password riêng
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status",    ignore = true)
    @Mapping(source = "userEmail", target = "email")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);

    // ── Custom method: map Set<Role> → Set<RoleInfo> ──────
    default Set<UserResponse.RoleInfo> mapRoles(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) return null;
        return roles.stream()
                .map(role -> UserResponse.RoleInfo.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .build())
                .collect(Collectors.toSet());
    }
}
```

### UserService

```java
@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository  userRepository;
    private final UserMapper      userMapper;
    private final PasswordEncoder passwordEncoder;

    // ── CREATE ────────────────────────────────────────────
    @Override
    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUserName())) {
            throw new RuntimeException("Username already exists");
        }

        // 1. Map DTO → Entity (id, roles, password, createdAt bị ignore)
        User user = userMapper.toUser(request);

        // 2. Set thủ công các field bị ignore
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(roleRepository.findByName("USER")));
        user.setCreatedAt(LocalDateTime.now());

        // 3. Lưu DB → Map Entity → Response
        return userMapper.toUserResponse(userRepository.save(user));
    }

    // ── READ ──────────────────────────────────────────────
    @Override
    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toUserResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userMapper.toUserResponseList(userRepository.findAll());
    }

    // ── UPDATE (PATCH) ────────────────────────────────────
    @Override
    public UserResponse updateUser(String id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Chỉ update field có giá trị, null → giữ nguyên
        userMapper.updateUser(user, request);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    // ── DELETE ────────────────────────────────────────────
    @Override
    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }
}
```

### UserController

```java
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    // POST /users — Tạo user mới
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @RequestBody @Valid UserCreationRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUser(request));
    }

    // GET /users — Lấy tất cả users
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // GET /users/{id} — Lấy user theo id
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // PATCH /users/{id} — Cập nhật một phần (null field → giữ nguyên)
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String id,
            @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    // DELETE /users/{id} — Xoá user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
```

### Flow toàn bộ (có kết quả)

```
── POST /users ──────────────────────────────────────────────────────────────

Request Body:
{ "userName": "nvminh162", "userEmail": "minh@mail.com", "password": "123456",
  "firstName": "Nguyen Van", "lastName": "Minh" }
    │
    ▼ userMapper.toUser()
User entity (sau mapper, trước Service xử lý):
{ id: null, username: "nvminh162", email: "minh@mail.com",
  password: null, status: "ACTIVE", roles: null, createdAt: null }
    │
    ▼ Service set thủ công (các field ignore)
{ id: null, username: "nvminh162", email: "minh@mail.com",
  password: "$2a$10$...", status: "ACTIVE", roles: [Role{USER}], createdAt: now() }
    │
    ▼ userRepository.save()  →  DB generate id
{ id: "uuid-abc", username: "nvminh162", ... }
    │
    ▼ userMapper.toUserResponse()
Response JSON trả về client (201 Created):
{
    "id":       "uuid-abc",
    "username": "nvminh162",
    "email":    "minh@mail.com",
    "fullName": "Nguyen Van Minh",   ← expression: firstName + " " + lastName
    "status":   "ACTIVE",
    "roles":    [{ "id": "r1", "name": "USER" }]   ← mapRoles()
}


── PATCH /users/uuid-abc ────────────────────────────────────────────────────

Request Body: { "firstName": "Minh" }   ← chỉ gửi firstName, còn lại null
    │
    ▼ userRepository.findById("uuid-abc")
User trong DB:
{ id: "uuid-abc", firstName: "Nguyen Van", lastName: "Minh",
  email: "minh@mail.com", roles: [ADMIN], password: "$2a$..." }
    │
    ▼ userMapper.updateUser() với @BeanMapping IGNORE
  firstName = "Minh"            ← update (có giá trị)
  lastName  = "Minh"            ← giữ nguyên (null → ignore)
  email     = "minh@mail.com"   ← giữ nguyên (null → ignore)
  roles     = [ADMIN]           ← giữ nguyên (@Mapping ignore)
  password  = "$2a$..."         ← giữ nguyên (@Mapping ignore)
    │
    ▼ userRepository.save() + toUserResponse()
Response JSON trả về client (200 OK):
{
    "id":       "uuid-abc",
    "username": "nvminh162",
    "email":    "minh@mail.com",
    "fullName": "Minh Minh",         ← firstName đã đổi thành "Minh"
    "status":   "ACTIVE",
    "roles":    [{ "id": "r1", "name": "ADMIN" }]
}
```

---

## 5. Bảng tra cứu nhanh

| Annotation / Feature | Dùng khi nào | Ví dụ |
|---|---|---|
| Không cần annotation | Field cùng tên, cùng type | `username → username` |
| `@Mapping(source, target)` | Tên field khác nhau | `userName → username` |
| `@Mapping(source = "obj.field")` | Lấy field từ nested object | `branch.id → branchId` |
| `@Mapping(target, ignore = true)` | Không map field (DB generate, set thủ công) | `id, createdAt` |
| `@Mapping(target, expression)` | Custom logic bằng Java code | gộp firstName + lastName |
| `@Mapping(target, constant)` | Set giá trị cố định | `status = "ACTIVE"` |
| `@Mapping(target, defaultValue)` | Giá trị mặc định khi source null | `point = "0"` |
| `@MappingTarget` | Update object đã có sẵn | PUT/PATCH |
| `@BeanMapping(IGNORE)` | PATCH — null field giữ nguyên giá trị cũ | partial update |
| `default method` | Custom logic phức tạp (map nested Set/List) | `mapRoles()` |
| `List<X> toList(List<Y>)` | Map collection tự động | `toUserResponseList()` |
| `uses = {OtherMapper.class}` | Tái sử dụng Mapper khác | `RoleMapper` trong `UserMapper` |
| `@AfterMapping` | Xử lý bổ sung sau khi map xong | set fullName, mask email |
| `@BeforeMapping` | Validate/chuẩn hóa data trước khi map | trim, toLowerCase |
| `imports = {...}` | Dùng class trong `expression` không cần full package | `Collectors.class` |
