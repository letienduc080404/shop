-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: shop_thoi_trang
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `ID_DanhMuc` bigint NOT NULL AUTO_INCREMENT,
  `TenDanhMuc` varchar(100) NOT NULL,
  PRIMARY KEY (`ID_DanhMuc`),
  UNIQUE KEY `UK_gqx6rbf2vm57i8b32yyx94l2` (`TenDanhMuc`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (1,'ÁO'),(4,'ÁO KHOÁC'),(3,'PHỤ KIỆN'),(2,'QUẦN'),(5,'VÁY');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customers`
--

DROP TABLE IF EXISTS `customers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customers` (
  `ID_KhachHang` bigint NOT NULL AUTO_INCREMENT,
  `NgayTao` datetime(6) DEFAULT NULL,
  `SoDienThoai` varchar(20) DEFAULT NULL,
  `Role` varchar(50) DEFAULT NULL,
  `HoTen` varchar(100) NOT NULL,
  `Email` varchar(150) NOT NULL,
  `DiaChi` varchar(255) DEFAULT NULL,
  `MatKhau` varchar(255) NOT NULL,
  `HangThanhVien` enum('Dong','Bac','Vang','KimCuong') DEFAULT NULL,
  PRIMARY KEY (`ID_KhachHang`),
  UNIQUE KEY `UK_bducw2hicmtd4bvhopbufmfkx` (`Email`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customers`
--

LOCK TABLES `customers` WRITE;
/*!40000 ALTER TABLE `customers` DISABLE KEYS */;
INSERT INTO `customers` VALUES (1,'2026-04-22 16:53:16.038485',NULL,'ROLE_ADMIN','Quản Trị Viên','admin@gmail.com',NULL,'$2a$10$xClmR0iaX5v5GxUmrK05Qe0ry.UYQ9g054v.5kw8e6TEs0Q5/yB66','Dong'),(2,'2026-04-22 16:53:16.203252','0987654321','ROLE_USER','Khách Hàng Thử Nghiệm','user@gmail.com','97 Man Thiện, Quận 9, TP.HCM','$2a$10$UAWRIcCn3QHe5PAKpLlOFucsHUjps8yFfmsZdtz/F5yV.2bNmu2b.','Dong'),(3,'2026-04-22 17:32:13.673662','0862764327','ROLE_USER','Lê Tiến Đức','letienduc247@gmail.com','97 Man Thiện, Quận 9, TP.HCM','$2a$10$beMp3nby4YMT/MjWmOpByeMHbypy7mrJRj.XsJTWHff3dFJ1pDezS','Dong'),(4,'2026-04-23 14:41:56.000577','0399449616','ROLE_USER','Phan Minh Hiền','phanminhhien2004@gmail.com','275 KVC','$2a$10$HpRGnCxyxUUiwNRbDoxk2OPLMU94l1Hf3Mf3rTQqrzuGjhQxAAd4C','Dong');
/*!40000 ALTER TABLE `customers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `GiaBan` decimal(12,2) NOT NULL,
  `SoLuong` int NOT NULL,
  `ID_BienThe` bigint NOT NULL,
  `ID_ChiTiet` bigint NOT NULL AUTO_INCREMENT,
  `ID_DonHang` bigint NOT NULL,
  PRIMARY KEY (`ID_ChiTiet`),
  KEY `FK5gxati27mwjr2q7ko9442j33q` (`ID_DonHang`),
  KEY `FK1tc971b9guiee8o9wedsk6rbp` (`ID_BienThe`),
  CONSTRAINT `FK1tc971b9guiee8o9wedsk6rbp` FOREIGN KEY (`ID_BienThe`) REFERENCES `product_variants` (`ID_BienThe`),
  CONSTRAINT `FK5gxati27mwjr2q7ko9442j33q` FOREIGN KEY (`ID_DonHang`) REFERENCES `orders` (`ID_DonHang`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (2500000.00,1,21,1,1);
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `TongTien` decimal(12,2) NOT NULL,
  `ID_DonHang` bigint NOT NULL AUTO_INCREMENT,
  `ID_KhachHang` bigint NOT NULL,
  `NgayDat` datetime(6) DEFAULT NULL,
  `SoDienThoaiNH` varchar(20) DEFAULT NULL,
  `MaDonHang` varchar(50) NOT NULL,
  `DiaChiNH` varchar(255) DEFAULT NULL,
  `PhuongThucThanhToan` enum('COD','ChuyenKhoan','ViDienTu') NOT NULL,
  `TrangThaiDonHang` enum('ChoXuLy','DangGiao','HoanThanh','DaHuy') DEFAULT NULL,
  PRIMARY KEY (`ID_DonHang`),
  UNIQUE KEY `UK_2lr90hpecrutdo3lcafitvopy` (`MaDonHang`),
  KEY `FKk23q64ouy3qeothxcpa4j8rgl` (`ID_KhachHang`),
  CONSTRAINT `FKk23q64ouy3qeothxcpa4j8rgl` FOREIGN KEY (`ID_KhachHang`) REFERENCES `customers` (`ID_KhachHang`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (2500000.00,1,2,'2026-04-23 14:19:03.021532','0987654321','ORD-530F9DC1','97 Man Thiện, Quận 9, TP.HCM','ChuyenKhoan','ChoXuLy'),(8680000.00,2,4,'2026-04-23 14:43:55.625518','0399449616','ORD-BB671CDA','275 KVC','ViDienTu','ChoXuLy'),(2450000.00,3,2,'2026-04-23 14:45:30.268696','0987654321','ORD-654DB7AC','97 Man Thiện, Quận 9, TP.HCM','ChuyenKhoan','ChoXuLy');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_images`
--

DROP TABLE IF EXISTS `product_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_images` (
  `ID_SanPham` bigint NOT NULL,
  `idAnh` bigint NOT NULL AUTO_INCREMENT,
  `DuongDan` varchar(1000) NOT NULL,
  PRIMARY KEY (`idAnh`),
  KEY `FKneyn5nfblhxqvsficej9fey1l` (`ID_SanPham`),
  CONSTRAINT `FKneyn5nfblhxqvsficej9fey1l` FOREIGN KEY (`ID_SanPham`) REFERENCES `products` (`ID_SanPham`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_images`
--

LOCK TABLES `product_images` WRITE;
/*!40000 ALTER TABLE `product_images` DISABLE KEYS */;
INSERT INTO `product_images` VALUES (6,4,'/images/uploads/4bc407f7-9933-4178-ad88-b771f004bddf_c11_01_aaamw39940ybr_mo-st-f1_2.webp');
/*!40000 ALTER TABLE `product_images` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_variants`
--

DROP TABLE IF EXISTS `product_variants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_variants` (
  `SoLuongTon` int NOT NULL,
  `ID_BienThe` bigint NOT NULL AUTO_INCREMENT,
  `ID_SanPham` bigint NOT NULL,
  `MauSac` varchar(50) NOT NULL,
  `KichThuoc` enum('XS','S','M','L','XL','OS') NOT NULL,
  PRIMARY KEY (`ID_BienThe`),
  UNIQUE KEY `UKnixtsjduyd4mhpynbn3rp3w3c` (`ID_SanPham`,`KichThuoc`,`MauSac`),
  CONSTRAINT `FKe4vjmuw6sco9vjy5qpk8l9a6y` FOREIGN KEY (`ID_SanPham`) REFERENCES `products` (`ID_SanPham`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_variants`
--

LOCK TABLES `product_variants` WRITE;
/*!40000 ALTER TABLE `product_variants` DISABLE KEYS */;
INSERT INTO `product_variants` VALUES (15,1,1,'Đen','XS'),(20,2,1,'Trắng','S'),(25,3,1,'Đỏ','M'),(11,4,2,'Xanh Rêu','S'),(12,5,2,'Đỏ','M'),(13,6,2,'Trắng','L'),(100,16,6,'Trắng','XS'),(100,17,6,'Trắng','S'),(100,18,6,'Trắng','M'),(100,19,6,'Trắng','L'),(100,20,6,'Trắng','XL'),(99,21,6,'Trắng','OS');
/*!40000 ALTER TABLE `product_variants` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `GiaNiemYet` decimal(12,2) NOT NULL,
  `ID_DanhMuc` bigint NOT NULL,
  `ID_SanPham` bigint NOT NULL AUTO_INCREMENT,
  `MaSKU` varchar(50) NOT NULL,
  `ChatLieu` varchar(100) DEFAULT NULL,
  `TenSanPham` varchar(200) NOT NULL,
  `HinhAnh` varchar(1000) DEFAULT NULL,
  `MoTa` text,
  `TrangThai` enum('ConHang','SapHetHang','NgungKinhDoanh') DEFAULT NULL,
  PRIMARY KEY (`ID_SanPham`),
  UNIQUE KEY `UK_1jl74t4pfvsdrd3ber2pf8ta4` (`MaSKU`),
  KEY `FK3erdodbwmcj0iad3fbr9hio3w` (`ID_DanhMuc`),
  CONSTRAINT `FK3erdodbwmcj0iad3fbr9hio3w` FOREIGN KEY (`ID_DanhMuc`) REFERENCES `categories` (`ID_DanhMuc`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (2450000.00,4,1,'BLZ-LINEN','Vải Linen Cao Cấp','Blazer Linen Cấu Trúc','https://lh3.googleusercontent.com/aida-public/AB6AXuAeNlzAbWJ35rW51Lw8N1-YgfrHODAyUW1zzl6KjXc7jqBUMi05DUmYkh2mNykWAzFaPE05NqqBd8IMuMStJZzgiUzIWcItROUJxsrXK1iGFoOutFw6EDx1nB9RXhuzE5-7F_FvDiDp9lWsWhIHfO62Uqu64amEhO8PpyxYrYln81-52UqTFnkZL5k8UdDGj_Ed33FZnh_XausLVexvoI5DaHTnjNG5KWDFi0Ne0s5VmWSX8J8XGd1m95IgMduLaGqM684P6uf16XDz','Sự tĩnh lặng tuyệt đối với kiểu dáng suông dài, tôn lên vẻ thanh lịch.','ConHang'),(1890000.00,5,2,'VY-LUA','Lụa tơ tằm','Váy Lụa Slip Dress','https://lh3.googleusercontent.com/aida-public/AB6AXuAhZYx0qVgpn6Cc8onETkxA7Rm2boi52pKTRPxwv1WPvR7VsLsE91XFgdq1apifQa9BehuwZLSZ8YDJkL3NKUmbZw1NXRnV6nTCucl6C4KtG-85x6uTNysJav8C26K97c9iAYH2OryfXjJxIccfbQLiAy5oTV-KjTbIBEpKXiqLFVGgaQGK2-tqVQBu3fmmJFKC0aP31dGUMMRxT0OJbkrOeKqCiOn15xykHMMmSjFoZQ9IB_xrpuGBQke5LDkUhZuyoMEbOYzMaiIH','Thanh lịch tối giản với chất liệu lụa tơ tằm.','ConHang'),(2500000.00,1,6,'SKU-331863',NULL,'Áo PoLo','/images/uploads/4bc407f7-9933-4178-ad88-b771f004bddf_c11_01_aaamw39940ybr_mo-st-f1_2.webp','Áo PoLo cho thanh niên trưởng thành ','ConHang');
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-23 21:59:56
