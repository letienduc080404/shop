package com.example.shop.dto.admin;

import java.math.BigDecimal;

public class MonthlyRevenuePointDto {
    private final String label;
    private final BigDecimal value;
    private final int percent;

    public MonthlyRevenuePointDto(String label, BigDecimal value, int percent) {
        this.label = label;
        this.value = value;
        this.percent = percent;
    }

    public String getLabel() {
        return label;
    }

    public BigDecimal getValue() {
        return value;
    }

    public int getPercent() {
        return percent;
    }
}

