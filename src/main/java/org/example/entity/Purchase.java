package org.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchases")
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_firm", nullable = false)
    private String productFirm;

    @Column(name = "original_price", nullable = false)
    private double originalPrice;

    @Column(name = "final_price", nullable = false)
    private double finalPrice;

    @Column(name = "applied_discount", nullable = false)
    private float appliedDiscount;

    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;

    @Column(name = "product_image_url")
    private String productImageURL;

    // Конструктор за замовчуванням для JPA
    protected Purchase() {}

    public Purchase(Long customerId, Long productId, String productName, String productFirm, 
                   double originalPrice, double finalPrice, float appliedDiscount, String productImageURL) {
        this.customerId = customerId;
        this.productId = productId;
        this.productName = productName;
        this.productFirm = productFirm;
        this.originalPrice = originalPrice;
        this.finalPrice = finalPrice;
        this.appliedDiscount = appliedDiscount;
        this.productImageURL = productImageURL;
        this.purchaseDate = LocalDateTime.now();
    }

    // Геттери та сеттери
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductFirm() {
        return productFirm;
    }

    public void setProductFirm(String productFirm) {
        this.productFirm = productFirm;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public float getAppliedDiscount() {
        return appliedDiscount;
    }

    public void setAppliedDiscount(float appliedDiscount) {
        this.appliedDiscount = appliedDiscount;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getProductImageURL() {
        return productImageURL;
    }

    public void setProductImageURL(String productImageURL) {
        this.productImageURL = productImageURL;
    }
} 