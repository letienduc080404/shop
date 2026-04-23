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
            // Create directory if it doesn't exist
            Path copyLocation = Paths.get(uploadDir);
            if (!Files.exists(copyLocation)) {
                Files.createDirectories(copyLocation);
            }

            // Generate a unique file name to avoid collisions
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path targetLocation = copyLocation.resolve(fileName);

            // Copy file to the target location
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Return the relative path to be stored in the database
            return "/images/uploads/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not store file " + file.getOriginalFilename() + ". Please try again!", e);
        }
    }

    public void deleteFile(String relativePath) {
        if (relativePath == null || !relativePath.startsWith("/images/uploads/")) {
            return; // Only delete files in our managed uploads directory
        }

        try {
            // Convert relative web path back to physical path
            String fileName = relativePath.replace("/images/uploads/", "");
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            
            // Delete the file if it exists
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Failed to delete physical file: " + relativePath);
        }
    }
}
