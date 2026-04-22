package com.example.shop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class CleanupTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    @Commit
    void cleanupImages() {
        int rows = jdbcTemplate.update("UPDATE products SET HinhAnh = NULL");
        System.out.println("====== CLEANUP SUCCESS: " + rows + " rows updated ======");
    }
}
