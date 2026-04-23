package com.example.shop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ánh xạ đường dẫn /images/uploads/** vào thư mục vật lý trên ổ cứng
        Path uploadDir = Paths.get("src/main/resources/static/images/uploads");
        String uploadPath = uploadDir.toFile().getAbsolutePath();
        
        // Sử dụng file: prefix để Spring biết đây là đường dẫn vật lý
        registry.addResourceHandler("/images/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
                
        // Khai báo lại cả thư mục images mặc định để đảm bảo không bị ghi đè
        Path imagesDir = Paths.get("src/main/resources/static/images");
        String imagesPath = imagesDir.toFile().getAbsolutePath();
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + imagesPath + "/");
    }
}
