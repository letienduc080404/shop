# DOMINO (Aura Shop)

DOMINO là một ứng dụng thương mại điện tử (E-commerce) được phát triển bằng Spring Boot, giao diện sử dụng Thymeleaf và Tailwind CSS, mang phong cách thiết kế tối giản (minimalism).

## 🛠️ Yêu Cầu Cài Đặt (Prerequisites)

Để chạy được dự án này, máy tính của bạn cần được cài đặt sẵn:
- **Java JDK 17** (hoặc mới hơn)
- **Maven**
- **MySQL Server** (Đang chạy trên cổng mặc định `3306`)

## 🗄️ Cấu Hình Cơ Sở Dữ Liệu (Quan Trọng)

Dự án này sử dụng cấu hình database được thiết lập cứng trong Java thay vì file `application.properties`. Để ứng dụng chạy được, bạn **bắt buộc** phải đảm bảo MySQL của bạn khớp với các cấu hình sau, hoặc sửa lại code cho phù hợp với máy của bạn.

Mở file `src/main/java/com/example/shop/config/DatabaseConfig.java` để xem hoặc thay đổi:

```java
dataSource.setUrl("jdbc:mysql://localhost:3306/shop_thoi_trang?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true");
dataSource.setUsername("root"); // Tên đăng nhập MySQL của bạn
dataSource.setPassword("123456"); // Mật khẩu cấu hình MySQL của bạn
```

**Lưu ý:** 
- Nhờ có thẻ `createDatabaseIfNotExist=true`, bạn không cần phải tạo database bằng tay. Nếu chưa có, Spring Boot sẽ tự động tạo database tên là `shop_thoi_trang`.
- Hibernate sẽ tự động tạo các bảng (Tables) thông qua tính năng `ddl-auto=update` trong `application.properties`.

## 🚀 Hướng Dẫn Chạy Dự Án

**Cách 1: Sử dụng Script có sẵn (Dành cho Windows)**
Chỉ cần mở terminal tại thư mục gốc của dự án và chạy:
```bash
./run.bat
```

**Cách 2: Sử dụng Maven**
Mở terminal tại thư mục gốc của dự án và chạy các lệnh sau:
```bash
mvn clean install
mvn spring-boot:run
```

Sau khi ứng dụng khởi động thành công, truy cập trang web tại: **http://localhost:8080**

## 🔑 Dữ Liệu Khởi Tạo (Mock Data)

Lần đầu tiên ứng dụng được chạy, nó sẽ tự động thêm một số dữ liệu mẫu vào (sản phẩm, danh mục,...) thông qua file `DataInitializer.java` để bạn có thể xem giao diện ngay lập tức.

Ngoài ra, hệ thống cũng tạo sẵn một tài khoản người dùng để test tính năng Đăng nhập / Thanh toán:
- **Email:** `user@gmail.com`
- **Mật khẩu:** `123`

## 📁 Cấu Trúc Dự Án Chính

- `src/main/java/com/example/shop/config`: Chứa cấu hình bảo mật xử lý đăng nhập (SecurityConfig) và cấu hình Database (DatabaseConfig).
- `src/main/java/com/example/shop/controller`: Chứa các controller điều hướng ứng dụng (Auth, Cart, Checkout, Product, Order).
- `src/main/resources/templates`: Toàn bộ giao diện HTML (Thymeleaf), sử dụng template fragments trong file `fragments/layout.html` để đồng bộ header/footer/assets toàn bộ website.
