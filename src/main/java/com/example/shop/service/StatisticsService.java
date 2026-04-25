package com.example.shop.service;

import com.example.shop.entity.enums.TrangThaiDonHang;
import com.example.shop.repository.OrderItemRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.OrderRepository.MonthlyDataView;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public StatisticsService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public Map<Integer, BigDecimal> getMonthlyRevenueForYear(int year) {
        List<MonthlyDataView> results = orderRepository.sumTongTienByYearAndTrangThaiDonHangNot(year, TrangThaiDonHang.DaHuy);
        return mapMonthlyData(results);
    }

    public Map<Integer, BigDecimal> getMonthlyProfitForYear(int year) {
        List<MonthlyDataView> results = orderItemRepository.sumLoiNhuanByYearAndTrangThaiDonHangNot(year, TrangThaiDonHang.DaHuy);
        return mapMonthlyData(results);
    }

    private Map<Integer, BigDecimal> mapMonthlyData(List<MonthlyDataView> results) {
        Map<Integer, BigDecimal> monthlyMap = new HashMap<>();
        // Initialize all months with 0
        for (int i = 1; i <= 12; i++) {
            monthlyMap.put(i, BigDecimal.ZERO);
        }
        for (MonthlyDataView view : results) {
            monthlyMap.put(view.getMonth(), view.getValue());
        }
        return monthlyMap;
    }

    public BigDecimal sumRevenueBetween(java.time.LocalDateTime from, java.time.LocalDateTime to) {
        return orderRepository.sumTongTienByNgayDatBetweenAndTrangThaiDonHangNot(from, to, TrangThaiDonHang.DaHuy);
    }

    public long countOrdersBetween(java.time.LocalDateTime from, java.time.LocalDateTime to) {
        return orderRepository.countByNgayDatBetweenAndTrangThaiDonHangNot(from, to, TrangThaiDonHang.DaHuy);
    }

    public long countUniqueCustomersBetween(java.time.LocalDateTime from, java.time.LocalDateTime to) {
        return orderRepository.countDistinctCustomersByNgayDatBetween(from, to);
    }
}
