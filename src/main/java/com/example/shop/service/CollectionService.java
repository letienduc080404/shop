package com.example.shop.service;

import com.example.shop.model.Collection;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CollectionService {
    private final CollectionsAndDiscountStorageService storageService;

    public CollectionService(CollectionsAndDiscountStorageService storageService) {
        this.storageService = storageService;
    }

    public List<Collection> getAllCollections() {
        return storageService.readCollections();
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
        storageService.writeCollections(collections);
    }
}
