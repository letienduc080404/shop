package com.example.shop.service;

import com.example.shop.entity.enums.DiscountType;
import com.example.shop.model.DiscountCode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DiscountCodeService {
    private final ConcurrentHashMap<Long, DiscountCode> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final CollectionsAndDiscountStorageService storageService;

    public DiscountCodeService(CollectionsAndDiscountStorageService storageService) {
        this.storageService = storageService;
        loadFromStorage();
    }

    public List<DiscountCode> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(DiscountCode::getId))
                .toList();
    }

    public DiscountCode findById(Long id) {
        return cloneCode(store.get(id));
    }

    public Optional<DiscountCode> findActiveByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String normalized = code.trim().toUpperCase();
        return store.values().stream()
                .filter(DiscountCode::isActive)
                .filter(item -> normalized.equals(item.getCode()))
                .findFirst()
                .map(this::cloneCode);
    }

    public DiscountCode save(DiscountCode input) {
        validate(input);
        String normalizedCode = input.getCode().trim().toUpperCase();

        boolean duplicated = store.values().stream()
                .anyMatch(item -> item.getCode().equals(normalizedCode)
                        && (input.getId() == null || !item.getId().equals(input.getId())));
        if (duplicated) {
            throw new IllegalArgumentException("Mã ưu đãi đã tồn tại.");
        }

        DiscountCode target = new DiscountCode();
        Long targetId = input.getId();
        if (targetId == null) {
            targetId = idGenerator.getAndIncrement();
        }
        target.setId(targetId);
        target.setCode(normalizedCode);
        target.setType(input.getType());
        target.setValue(input.getValue());
        target.setActive(input.isActive());

        store.put(target.getId(), target);
        persistToStorage();
        return cloneCode(target);
    }

    public BigDecimal calculateDiscount(DiscountCode discountCode, BigDecimal subtotal) {
        if (discountCode == null || subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal rawDiscount = discountCode.getType() == DiscountType.PERCENT
                ? subtotal.multiply(discountCode.getValue()).divide(BigDecimal.valueOf(100))
                : discountCode.getValue();
        if (rawDiscount.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return rawDiscount.min(subtotal);
    }

    private void validate(DiscountCode input) {
        if (input.getCode() == null || input.getCode().isBlank()) {
            throw new IllegalArgumentException("Mã ưu đãi không được để trống.");
        }
        if (input.getType() == null) {
            throw new IllegalArgumentException("Vui lòng chọn loại ưu đãi.");
        }
        if (input.getValue() == null || input.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá trị ưu đãi phải lớn hơn 0.");
        }
        if (input.getType() == DiscountType.PERCENT && input.getValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Giảm theo phần trăm không được lớn hơn 100.");
        }
    }

    private DiscountCode cloneCode(DiscountCode source) {
        if (source == null) {
            return null;
        }
        DiscountCode clone = new DiscountCode();
        clone.setId(source.getId());
        clone.setCode(source.getCode());
        clone.setType(source.getType());
        clone.setValue(source.getValue());
        clone.setActive(source.isActive());
        return clone;
    }

    private void loadFromStorage() {
        List<DiscountCode> savedCodes = storageService.readDiscountCodes();
        long maxId = 0;
        for (DiscountCode code : savedCodes) {
            if (code.getId() == null) {
                continue;
            }
            store.put(code.getId(), cloneCode(code));
            if (code.getId() > maxId) {
                maxId = code.getId();
            }
        }
        idGenerator.set(maxId + 1);
    }

    private void persistToStorage() {
        List<DiscountCode> allCodes = new ArrayList<>(store.values());
        allCodes.sort(Comparator.comparing(DiscountCode::getId));
        storageService.writeDiscountCodes(allCodes);
    }
}
