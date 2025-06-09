package org.example.dto;

public class PurchaseRequest {
    private Long customerId;
    private Long productId;

    // Конструкторы
    public PurchaseRequest() {}

    public PurchaseRequest(Long customerId, Long productId) {
        this.customerId = customerId;
        this.productId = productId;
    }

    // Геттеры и сеттеры
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
} 