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
