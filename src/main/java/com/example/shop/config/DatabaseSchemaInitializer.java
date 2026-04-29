package com.example.shop.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSchemaInitializer implements CommandLineRunner {
    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        Integer columnCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'products'
                  AND COLUMN_NAME = 'GiaKhuyenMai'
                """,
                Integer.class);

        if (columnCount != null && columnCount == 0) {
            jdbcTemplate.execute("ALTER TABLE products ADD COLUMN GiaKhuyenMai DECIMAL(12,2) NULL");
            System.out.println("--- Added column products.GiaKhuyenMai ---");
        }
    }
}
