package com.example.shop.repository;

import com.example.shop.entity.Customer;
import com.example.shop.entity.enums.HangThanhVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);

    long countByHangThanhVien(HangThanhVien hangThanhVien);

    @Query("""
        select c
        from Customer c
        where (:q is null)
           or (lower(c.hoTen) like lower(concat('%', :q, '%')))
           or (lower(c.email) like lower(concat('%', :q, '%')))
           or (concat('', c.idKhachHang) like concat('%', :q, '%'))
    """)
    Page<Customer> searchForAdmin(@Param("q") String q, Pageable pageable);
}
