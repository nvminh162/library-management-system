# ==========================================
# Giai đoạn 1: Build ứng dụng bằng Maven
# https://hub.docker.com/_/maven
# ==========================================
# Sử dụng image Maven 3.9.12 với JDK 21 (Eclipse Temurin) làm môi trường build
FROM maven:3.9.12-eclipse-temurin-21 AS build
# Đặt thư mục làm việc trong container là /app
WORKDIR /app
# Sao chép toàn bộ mã nguồn từ build context (thư mục hiện tại) vào /app/discover-server trong container
COPY . ./discover-server
# Chuyển vào thư mục dự án discover-server để thực hiện build
WORKDIR /app/discover-server
# Chạy lệnh Maven để build ứng dụng, bỏ qua việc chạy test để tăng tốc độ build
RUN mvn clean package -DskipTests

# ==========================================
# Giai đoạn 2: Chạy ứng dụng bằng OpenJDK 21
# https://hub.docker.com/_/eclipse-temurin/tags?name=21
# ==========================================
# Sử dụng image JRE 21 (Jammy = Ubuntu 22.04) - nhẹ hơn JDK vì chỉ cần môi trường chạy, không cần biên dịch
FROM eclipse-temurin:21-jre-jammy
# Đặt thư mục làm việc trong container là /app
WORKDIR /app
# Sao chép file JAR đã build từ giai đoạn 1 (build stage) sang giai đoạn hiện tại
COPY --from=build /app/discover-server/target/*.jar discover-server.jar
# Khai báo cổng 8761 mà ứng dụng lắng nghe (cổng mặc định của Eureka Discovery Server)
EXPOSE 8761
# Lệnh khởi động ứng dụng khi container chạy
ENTRYPOINT ["java", "-jar", "discover-server.jar"]
