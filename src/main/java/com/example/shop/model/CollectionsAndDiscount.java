package com.example.shop.model;

import java.util.ArrayList;
import java.util.List;

public class CollectionsAndDiscount {
    private List<Collection> collections = new ArrayList<>();
    private List<DiscountCode> discountCodes = new ArrayList<>();

    public List<Collection> getCollections() {
        return collections;
    }

    public void setCollections(List<Collection> collections) {
        this.collections = collections;
    }

    public List<DiscountCode> getDiscountCodes() {
        return discountCodes;
    }

    public void setDiscountCodes(List<DiscountCode> discountCodes) {
        this.discountCodes = discountCodes;
    }
}
