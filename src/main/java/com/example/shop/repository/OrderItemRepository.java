package com.example.shop.repository;

import com.example.shop.entity.OrderItem;
import com.example.shop.entity.enums.TrangThaiDonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    boolean existsByProductVariant_Product_IdSanPham(Long idSanPham);
    long deleteByOrder_Customer_IdKhachHang(Long customerId);

    interface ProductSalesAggView {
        Long getProductId();
        Long getDaBan();
        BigDecimal getDoanhThu();
    }

    @Query("""
        select
            p.idSanPham as productId,
            coalesce(sum(oi.soLuong), 0) as daBan,
            coalesce(sum(oi.giaBan * oi.soLuong), 0) as doanhThu
        from OrderItem oi
        join oi.order o
        join oi.productVariant pv
        join pv.product p
        where o.ngayDat >= :from and o.ngayDat < :to
          and o.trangThaiDonHang = :status
        group by p.idSanPham
        order by doanhThu desc
    """)
    List<ProductSalesAggView> topProductsByRevenue(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("status") TrangThaiDonHang status,
            Pageable pageable
    );

    @Query("""
        select
            p.idSanPham as productId,
            coalesce(sum(oi.soLuong), 0) as daBan,
            coalesce(sum(oi.giaBan * oi.soLuong), 0) as doanhThu
        from OrderItem oi
        join oi.order o
        join oi.productVariant pv
        join pv.product p
        where o.ngayDat >= :from and o.ngayDat < :to
          and o.trangThaiDonHang <> :excludedStatus
        group by p.idSanPham
        order by doanhThu desc
    """)
    List<ProductSalesAggView> topProductsByRevenueDonHangNot(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("excludedStatus") TrangThaiDonHang excludedStatus,
            Pageable pageable
    );

    @Query("""
        select coalesce(sum((oi.giaBan - coalesce(pv.giaVon, 0)) * oi.soLuong), 0)
        from OrderItem oi
        join oi.order o
        join oi.productVariant pv
        where o.ngayDat >= :from and o.ngayDat < :to
          and o.trangThaiDonHang = :status
    """)
    BigDecimal sumLoiNhuanByNgayDatBetweenAndTrangThai(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("status") TrangThaiDonHang status
    );

    @Query("""
        select coalesce(sum((oi.giaBan - coalesce(pv.giaVon, 0)) * oi.soLuong), 0)
        from OrderItem oi
        join oi.order o
        join oi.productVariant pv
        where o.ngayDat >= :from and o.ngayDat < :to
          and o.trangThaiDonHang <> :excludedStatus
    """)
    BigDecimal sumLoiNhuanByNgayDatBetweenAndTrangThaiDonHangNot(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("excludedStatus") TrangThaiDonHang excludedStatus
    );

    @Query("""
        select month(o.ngayDat) as month, coalesce(sum((oi.giaBan - coalesce(pv.giaVon, 0)) * oi.soLuong), 0) as value
        from OrderItem oi
        join oi.order o
        join oi.productVariant pv
        where year(o.ngayDat) = :year and o.trangThaiDonHang <> :excludedStatus
        group by month(o.ngayDat)
    """)
    List<OrderRepository.MonthlyDataView> sumLoiNhuanByYearAndTrangThaiDonHangNot(@Param("year") int year, @Param("excludedStatus") TrangThaiDonHang excludedStatus);
}
