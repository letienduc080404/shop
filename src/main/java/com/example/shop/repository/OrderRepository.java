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
import java.util.List;

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
        where o.trangThaiDonHang = :status
    """)
    BigDecimal sumTongTienByTrangThai(TrangThaiDonHang status);

    @Query("""
        select coalesce(sum(o.tongTien), 0)
        from Order o
        where o.ngayDat >= :from and o.ngayDat < :to
          and o.trangThaiDonHang = :status
    """)
    BigDecimal sumTongTienByNgayDatBetweenAndTrangThai(LocalDateTime from, LocalDateTime to, TrangThaiDonHang status);

    @Query("""
        select count(distinct o.customer.idKhachHang)
        from Order o
        where o.ngayDat >= :from
    """)
    long countDistinctCustomersByNgayDatAfter(LocalDateTime from);

    List<Order> findTop5ByCustomer_IdKhachHangOrderByNgayDatDesc(Long idKhachHang);

    interface CustomerOrderAggView {
        Long getCustomerId();
        BigDecimal getTongChiTieu();
        Long getSoDonHang();
        LocalDateTime getNgayDonGanNhat();
    }

    @Query("""
        select 
            o.customer.idKhachHang as customerId,
            coalesce(sum(o.tongTien), 0) as tongChiTieu,
            count(o.idDonHang) as soDonHang,
            max(o.ngayDat) as ngayDonGanNhat
        from Order o
        where o.customer.idKhachHang in :customerIds
        group by o.customer.idKhachHang
    """)
    List<CustomerOrderAggView> aggregateByCustomerIds(List<Long> customerIds);
}
