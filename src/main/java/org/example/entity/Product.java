package org.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price", nullable = false)
    private float price;

    @Column(name = "firm", nullable = false)
    private String firm;

    @Column(name = "max_discount_percentage", nullable = false)
    private float maxDiscountPercentage;

    @Column(name = "quantity", nullable = false)
    private int quantity = 0;

    @Column(name = "image_url")
    private String imageURL;

    // Конструктор по умолчанию для JPA
    protected Product() {}

    public Product(String name, float price, String firm, float maxDiscountPercentage) {
        this.name = name;
        this.price = price;
        this.firm = firm;
        this.maxDiscountPercentage = maxDiscountPercentage;
        this.quantity = 0;
    }

    public Product(String name, float price, String firm, float maxDiscountPercentage, int quantity) {
        this.name = name;
        this.price = price;
        this.firm = firm;
        this.maxDiscountPercentage = maxDiscountPercentage;
        this.quantity = quantity;
    }

    public Product(String name, float price, String firm, float maxDiscountPercentage, int quantity, String imageURL) {
        this.name = name;
        this.price = price;
        this.firm = firm;
        this.maxDiscountPercentage = maxDiscountPercentage;
        this.quantity = quantity;
        this.imageURL = imageURL;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getFirm() {
        return firm;
    }

    public void setFirm(String firm) {
        this.firm = firm;
    }

    public float getMaxDiscountPercentage() {
        return maxDiscountPercentage;
    }

    public void setMaxDiscountPercentage(float maxDiscountPercentage) {
        this.maxDiscountPercentage = maxDiscountPercentage;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
