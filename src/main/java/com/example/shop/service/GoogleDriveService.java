package com.example.shop.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.UUID;

@Service
public class GoogleDriveService {

    @Value("${google.drive.client-id}")
    private String clientId;

    @Value("${google.drive.client-secret}")
    private String clientSecret;

    @Value("${google.drive.refresh-token}")
    private String refreshToken;

    @Value("${google.drive.folder-id}")
    private String folderId;

    private Drive getDriveService() throws Exception {
        // Sử dụng OAuth2 User Credentials với Refresh Token
        UserCredentials credentials = UserCredentials.newBuilder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(refreshToken)
                .build();

        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("AuraShop")
                .build();
    }

    /**
     * Upload từ file upload (khi thêm/sửa sản phẩm)
     */
    public String uploadFile(MultipartFile multipartFile) throws IOException {
        String originalFilename = multipartFile.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String safeFileName = UUID.randomUUID().toString() + extension;

        java.io.File tempFile = null;
        try {
            tempFile = java.io.File.createTempFile("aura_upload_", extension);
            multipartFile.transferTo(tempFile);
            
            String contentType = multipartFile.getContentType();
            if (contentType == null) contentType = "application/octet-stream";

            return uploadToDrive(safeFileName, tempFile, contentType);
        } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
            throw new IOException("Lỗi upload Drive (OAuth2): " + e.getDetails().getMessage(), e);
        } catch (Exception e) {
            throw new IOException("Lỗi kết nối Google Drive (OAuth2): " + e.getMessage(), e);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * Upload từ Path local (cho việc migration)
     */
    public String uploadFile(Path filePath) throws IOException {
        String fileName = filePath.getFileName().toString();
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) contentType = "application/octet-stream";

        try {
            return uploadToDrive(fileName, filePath.toFile(), contentType);
        } catch (Exception e) {
            throw new IOException("Lỗi upload migration Drive: " + e.getMessage(), e);
        }
    }

    private String uploadToDrive(String fileName, java.io.File fileToUpload, String contentType) throws Exception {
        Drive service = getDriveService();

        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        fileMetadata.setParents(Collections.singletonList(folderId));

        FileContent mediaContent = new FileContent(contentType, fileToUpload);

        File file = service.files()
                .create(fileMetadata, mediaContent)
                .setSupportsAllDrives(true)
                .setFields("id")
                .execute();

        // Thiết lập quyền xem cho mọi người (để lấy được link ảnh)
        try {
            Permission permission = new Permission().setType("anyone").setRole("reader");
            service.permissions().create(file.getId(), permission)
                    .setSupportsAllDrives(true)
                    .execute();
        } catch (IOException | RuntimeException e) {
            System.err.println("GDRIVE WARNING: Không thể set quyền public cho file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("GDRIVE ERROR: Lỗi không xác định: " + e.getMessage());
        }

        return file.getId();
    }

    public void deleteFile(String fileId) {
        if (fileId == null || fileId.startsWith("/") || fileId.startsWith("http")) return;
        try {
            Drive service = getDriveService();
            service.files().delete(fileId).setSupportsAllDrives(true).execute();
            System.out.println("GDRIVE: Đã xóa file ID: " + fileId);
        } catch (IOException | RuntimeException e) {
            System.err.println("GDRIVE UPLOAD ERROR (createProduct): " + e.getMessage());
        } catch (Exception e) {
            System.err.println("GDRIVE CRITICAL ERROR (createProduct): " + e.getMessage());
        }
    }
}
