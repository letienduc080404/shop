package com.example.shop.chatbot;

import java.math.BigDecimal;

public class ProductChatDto {
    private final Long id;
    private final String name;
    private final String category;
    private final BigDecimal price;
    private final int totalStock;

    public ProductChatDto(Long id, String name, String category, BigDecimal price, int totalStock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.totalStock = totalStock;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getTotalStock() {
        return totalStock;
    }
}
