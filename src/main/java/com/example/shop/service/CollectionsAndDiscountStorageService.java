package com.example.shop.service;

import com.example.shop.model.Collection;
import com.example.shop.model.CollectionsAndDiscount;
import com.example.shop.model.DiscountCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class CollectionsAndDiscountStorageService {
    private static final Path STORAGE_PATH = Paths.get("CollectionsAndDiscount.json");
    private static final Path LEGACY_COLLECTIONS_PATH = Paths.get("collections.json");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public synchronized void initializeStorage() {
        if (Files.exists(STORAGE_PATH)) {
            return;
        }
        CollectionsAndDiscount data = new CollectionsAndDiscount();
        if (Files.exists(LEGACY_COLLECTIONS_PATH)) {
            try {
                List<Collection> oldCollections = objectMapper.readValue(
                        LEGACY_COLLECTIONS_PATH.toFile(),
                        new TypeReference<List<Collection>>() {});
                data.setCollections(oldCollections);
            } catch (IOException ignored) {
                data.setCollections(new ArrayList<>());
            }
        }
        writeData(data);
    }

    public synchronized List<Collection> readCollections() {
        return new ArrayList<>(readData().getCollections());
    }

    public synchronized void writeCollections(List<Collection> collections) {
        CollectionsAndDiscount data = readData();
        data.setCollections(collections != null ? collections : new ArrayList<>());
        writeData(data);
    }

    public synchronized List<DiscountCode> readDiscountCodes() {
        return new ArrayList<>(readData().getDiscountCodes());
    }

    public synchronized void writeDiscountCodes(List<DiscountCode> discountCodes) {
        CollectionsAndDiscount data = readData();
        data.setDiscountCodes(discountCodes != null ? discountCodes : new ArrayList<>());
        writeData(data);
    }

    private CollectionsAndDiscount readData() {
        if (!Files.exists(STORAGE_PATH)) {
            return new CollectionsAndDiscount();
        }
        try {
            CollectionsAndDiscount data = objectMapper.readValue(STORAGE_PATH.toFile(), CollectionsAndDiscount.class);
            if (data.getCollections() == null) {
                data.setCollections(new ArrayList<>());
            }
            if (data.getDiscountCodes() == null) {
                data.setDiscountCodes(new ArrayList<>());
            }
            return data;
        } catch (IOException e) {
            return new CollectionsAndDiscount();
        }
    }

    private void writeData(CollectionsAndDiscount data) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(STORAGE_PATH.toFile(), data);
        } catch (IOException e) {
            throw new RuntimeException("Cannot write CollectionsAndDiscount.json", e);
        }
    }
}
