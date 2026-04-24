package com.example.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final String uploadDir = "src/main/resources/static/images/uploads/";

    public String storeFile(MultipartFile file) {
        try {
            // Tạo thư mục lưu nếu chưa tồn tại
            Path copyLocation = Paths.get(uploadDir);
            if (!Files.exists(copyLocation)) {
                Files.createDirectories(copyLocation);
            }

            // Tạo tên file duy nhất để tránh trùng
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path targetLocation = copyLocation.resolve(fileName);

            // Sao chép file vào vị trí đích
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Trả về đường dẫn tương đối để lưu vào CSDL
            return "/images/uploads/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not store file " + file.getOriginalFilename() + ". Please try again!", e);
        }
    }

    public void deleteFile(String relativePath) {
        if (relativePath == null || !relativePath.startsWith("/images/uploads/")) {
            return; // Chỉ xoá file trong thư mục upload do hệ thống quản lý
        }

        try {
            // Đổi đường dẫn web tương đối về đường dẫn vật lý
            String fileName = relativePath.replace("/images/uploads/", "");
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            
            // Xoá file nếu tồn tại
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Failed to delete physical file: " + relativePath);
        }
    }
}
