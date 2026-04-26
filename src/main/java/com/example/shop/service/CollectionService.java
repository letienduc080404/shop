package com.example.shop.service;

import com.example.shop.model.Collection;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CollectionService {
    private final String FILE_PATH = "collections.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Collection> getAllCollections() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return new ArrayList<>();
        try {
            return objectMapper.readValue(file, new TypeReference<List<Collection>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Collection getCollectionById(String id) {
        return getAllCollections().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void saveCollection(String name, String description, String imageUrl, List<Long> productIds) {
        List<Collection> collections = getAllCollections();
        Collection collection = new Collection();
        collection.setId(UUID.randomUUID().toString());
        collection.setName(name);
        collection.setDescription(description);
        collection.setImageUrl(imageUrl);
        collection.setProductIds(productIds != null ? productIds : new ArrayList<>());
        
        collections.add(collection);
        saveToFile(collections);
    }

    public void updateCollection(String id, String name, String description, String imageUrl, List<Long> productIds) {
        List<Collection> collections = getAllCollections();
        for (Collection c : collections) {
            if (c.getId().equals(id)) {
                c.setName(name);
                c.setDescription(description);
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    c.setImageUrl(imageUrl);
                }
                c.setProductIds(productIds != null ? productIds : new ArrayList<>());
                break;
            }
        }
        saveToFile(collections);
    }

    public void deleteCollection(String id) {
        List<Collection> collections = getAllCollections();
        collections.removeIf(c -> c.getId().equals(id));
        saveToFile(collections);
    }

    private void saveToFile(List<Collection> collections) {
        try {
            objectMapper.writeValue(new File(FILE_PATH), collections);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
