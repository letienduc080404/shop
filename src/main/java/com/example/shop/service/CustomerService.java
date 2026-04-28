package com.example.shop.service;

import com.example.shop.dto.admin.CustomerAdminRowDto;
import com.example.shop.entity.Customer;
import com.example.shop.entity.Order;
import com.example.shop.entity.enums.HangThanhVien;
import com.example.shop.entity.enums.TrangThaiDonHang;
import com.example.shop.repository.CustomerRepository;
import com.example.shop.repository.OrderItemRepository;
import com.example.shop.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public CustomerService(CustomerRepository customerRepository, OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional(readOnly = true)
    public Page<CustomerAdminRowDto> getCustomersForAdmin(String q, HangThanhVien hang, int page, int size) {
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "ngayTao")
        );

        String keyword = (q == null || q.isBlank()) ? null : q.trim();
        Page<Customer> customersPage = customerRepository.searchForAdmin(keyword, hang, pageable);

        List<Long> customerIds = customersPage.getContent().stream()
                .map(Customer::getIdKhachHang)
                .toList();

        Map<Long, OrderRepository.CustomerOrderAggView> aggMap = new HashMap<>();
        if (!customerIds.isEmpty()) {
            for (OrderRepository.CustomerOrderAggView agg : orderRepository.aggregateByCustomerIds(customerIds)) {
                aggMap.put(agg.getCustomerId(), agg);
            }
        }

        return customersPage.map(c -> {
            CustomerAdminRowDto dto = new CustomerAdminRowDto(
                    c.getIdKhachHang(),
                    c.getHoTen(),
                    c.getEmail(),
                    c.getHangThanhVien()
            );
            dto.setAnhDaiDien(c.getAnhDaiDien());
            OrderRepository.CustomerOrderAggView agg = aggMap.get(c.getIdKhachHang());
            if (agg != null) {
                BigDecimal spending = agg.getTongChiTieu();
                dto.setTongChiTieu(spending != null ? spending : BigDecimal.ZERO);
                Long count = agg.getSoDonHang();
                dto.setSoDonHang(count != null ? count : 0L);
                dto.setNgayDonGanNhat(agg.getNgayDonGanNhat());
            }
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public long getTotalCustomers() {
        return customerRepository.count();
    }

    @Transactional(readOnly = true)
    public long getDiamondCustomers() {
        return customerRepository.countByHangThanhVien(HangThanhVien.KimCuong);
    }

    @Transactional(readOnly = true)
    public BigDecimal getAverageOrderValue() {
        long completedCount = orderRepository.countByTrangThaiDonHang(TrangThaiDonHang.HoanThanh);
        if (completedCount <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = orderRepository.sumTongTienByTrangThai(TrangThaiDonHang.HoanThanh);
        if (total == null) {
            return BigDecimal.ZERO;
        }
        return total.divide(BigDecimal.valueOf(completedCount), 2, java.math.RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public double getRetentionRatePercent(int activeDays) {
        long totalCustomers = getTotalCustomers();
        if (totalCustomers == 0) {
            return 0.0;
        }
        LocalDateTime from = LocalDateTime.now().minusDays(Math.max(activeDays, 1));
        long activeCustomers = orderRepository.countDistinctCustomersByNgayDatAfter(from);
        return (activeCustomers * 100.0) / totalCustomers;
    }

    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        if (id == null) return null;
        return customerRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Order> getLatestOrdersOfCustomer(Long customerId, int limit) {
        if (customerId == null) return List.of();
        int max = Math.max(1, Math.min(limit, 20));
        List<Order> orders = orderRepository.findTop5ByCustomer_IdKhachHangOrderByNgayDatDesc(customerId);
        return orders.size() <= max ? orders : orders.subList(0, max);
    }

    @Transactional
    public boolean deleteCustomerById(Long id) {
        if (id == null || !customerRepository.existsById(id)) {
            return false;
        }
        // Xóa dữ liệu phụ thuộc trước để tránh lỗi khóa ngoại.
        orderItemRepository.deleteByOrder_Customer_IdKhachHang(id);
        orderRepository.deleteByCustomer_IdKhachHang(id);
        customerRepository.deleteById(id);
        return true;
    }
}

