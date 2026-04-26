Đã bổ sung thêm tính năng, hãy xem ở dưới
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

  
  #git pull
git pull origin main


#git push
git add .

git commit -m "Mô tả việc mình làm"

git push origin main

# 📦 Project Updates & Features

## 🔍 Tính năng mới

### 1. Thanh tìm kiếm
- Đã thêm thanh tìm kiếm giúp người dùng tìm sản phẩm nhanh hơn.

### 2. Hỗ trợ khách hàng (Live Chat)
- Nút **Yêu cầu hỗ trợ** sẽ mở live chat với admin.
- Admin có thể trả lời user thông qua mục **Hỗ trợ khách hàng** trên dashboard.

---

## 🛠️ Cập nhật phía Admin

### 1. Quản lý đơn hàng
- Admin có thể:
  - Hủy đơn
  - Chuẩn bị hàng  
- Thao tác thông qua nút **3 chấm dọc** trong mỗi order.

### 2. Trang Settings
- Giao diện đơn giản hơn khi truy cập.

---

## 👤 Cập nhật phía User

### 1. Quản lý đơn hàng
- Xem lịch sử đơn hàng tại:
  - **Lịch sử đơn hàng**
- Có thể:
  - ❌ Hủy đơn nếu trạng thái: `Chờ xử lý`
  - ✅ Xác nhận đã nhận hàng nếu trạng thái: `Đang giao` → chuyển sang `Hoàn thành`

### 2. Thông tin cá nhân
- Người dùng có thể chỉnh sửa thông tin:
  - Nhấn vào **tên đăng nhập (góc trên bên phải)**

---

## 🧭 Cập nhật Navigation Menu

### 🔄 Thay đổi menu:
| Cũ | Mới |
|----|-----|
| COLLECTION | HOME |
| NEW ARRIVE | NEW ARRIVE |
| EDITORIAL | COLLECTION |

---

## 📌 Chức năng từng mục

### 🏠 HOME
- Hiển thị toàn bộ sản phẩm.

### 🆕 NEW ARRIVE
- Hiển thị sản phẩm mới:
  - Có tag **"New"** trên ảnh.
  - Giới hạn: **8 sản phẩm mới nhất**
- ⚠️ Không thay đổi database để tránh lỗi đồng bộ.

### 🗂️ COLLECTION
- Hiển thị bộ sưu tập.
- Admin quản lý qua:
  - Tag **Collection** trong Dashboard
- Dữ liệu lưu tại:
```json
collection.json
```
- ⚠️ Tương tự NEW ARIRIVE, không thay đổi database để tránh lỗi đồng bộ.
