package com.example.shop.service;

import com.example.shop.dto.CartItem;
import com.example.shop.entity.Customer;
import com.example.shop.entity.Order;
import com.example.shop.entity.OrderItem;
import com.example.shop.entity.ProductVariant;
import com.example.shop.entity.enums.KichThuoc;
import com.example.shop.entity.enums.PhuongThucThanhToan;
import com.example.shop.entity.enums.TrangThaiDonHang;
import com.example.shop.repository.OrderItemRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.ProductVariantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository productVariantRepository;

    public OrderService(OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductVariantRepository productVariantRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @Transactional(readOnly = true)
    public Page<Order> getOrdersForAdmin(TrangThaiDonHang status, Pageable pageable) {
        if (status == null) {
            return orderRepository.findAll(pageable);
        }
        return orderRepository.findByTrangThaiDonHang(status, pageable);
    }

    @Transactional(readOnly = true)
    public long getTotalOrdersCount() {
        return orderRepository.count();
    }

    @Transactional(readOnly = true)
    public long getOrdersCountByStatus(TrangThaiDonHang status) {
        return orderRepository.countByTrangThaiDonHang(status);
    }

    @Transactional(readOnly = true)
    public BigDecimal getRevenueForMonth(YearMonth month) {
        LocalDateTime from = month.atDay(1).atStartOfDay();
        LocalDateTime to = month.plusMonths(1).atDay(1).atStartOfDay();
        return orderRepository.sumTongTienByNgayDatBetweenAndTrangThaiDonHangNot(from, to, TrangThaiDonHang.DaHuy);
    }

    @Transactional
    public Order createOrder(Customer customer, List<CartItem> cartItems,
            String diaChi, String soDienThoai,
            PhuongThucThanhToan phuongThuc) {

        Order order = new Order();
        order.setCustomer(customer);
        order.setMaDonHang("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setNgayDat(LocalDateTime.now());
        order.setPhuongThucThanhToan(phuongThuc);
        order.setTrangThaiDonHang(TrangThaiDonHang.ChoXuLy);
        order.setDiaChiNH(diaChi);
        order.setSoDienThoaiNH(soDienThoai);

        double total = cartItems.stream().mapToDouble(item -> item.getGia() * item.getSoLuong()).sum();
        order.setTongTien(BigDecimal.valueOf(total));

        Order savedOrder = orderRepository.save(order);

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);

            // Thử tìm biến thể sản phẩm phù hợp (theo size)
            ProductVariant variant = productVariantRepository.findByProduct_IdSanPhamAndKichThuoc(
                    cartItem.getIdSanPham(),
                    KichThuoc.valueOf(cartItem.getKichThuoc())).stream().findFirst().orElse(null);

            if (variant != null) {
                orderItem.setProductVariant(variant);
                orderItem.setSoLuong(cartItem.getSoLuong());
                orderItem.setGiaBan(BigDecimal.valueOf(cartItem.getGia()));
                orderItemRepository.save(orderItem);

                // (Tuỳ chọn) cập nhật tồn kho
                variant.setSoLuongTon(variant.getSoLuongTon() - cartItem.getSoLuong());
                productVariantRepository.save(variant);
            }
        }

        return savedOrder;
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Transactional
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }
}
