# ============================
# Stage 1: Build
# ============================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper & pom.xml trước để tận dụng Docker layer cache
COPY .mvn/ .mvn/
COPY mvnw mvnw.cmd pom.xml ./

# Tải trước dependencies (cache layer này nếu pom.xml không đổi)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy toàn bộ source code
COPY src/ src/

# Build JAR file, bỏ qua test để tăng tốc độ đóng gói
RUN ./mvnw package -DskipTests -B

# ============================
# Stage 2: Runtime
# ============================
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# Tạo user non-root để tăng cường bảo mật
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy file JAR từ stage builder
COPY --from=builder /app/target/*.jar app.jar

# Đổi quyền sở hữu file JAR cho non-root user
RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8083

# Đặt múi giờ mặc định Asia/Ho_Chi_Minh khi chạy container
ENTRYPOINT ["java", "-Duser.timezone=Asia/Ho_Chi_Minh", "-jar", "app.jar"]
