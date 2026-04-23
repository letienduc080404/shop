package com.example.shop.repository;

import com.example.shop.entity.Order;
import com.example.shop.entity.enums.TrangThaiDonHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByMaDonHang(String maDonHang);

    @EntityGraph(attributePaths = {"customer"})
    Page<Order> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"customer"})
    Page<Order> findByTrangThaiDonHang(TrangThaiDonHang trangThaiDonHang, Pageable pageable);

    long countByTrangThaiDonHang(TrangThaiDonHang trangThaiDonHang);

    @Query("""
        select coalesce(sum(o.tongTien), 0)
        from Order o
        where o.ngayDat >= :from and o.ngayDat < :to
          and o.trangThaiDonHang = :status
    """)
    BigDecimal sumTongTienByNgayDatBetweenAndTrangThai(LocalDateTime from, LocalDateTime to, TrangThaiDonHang status);
}
