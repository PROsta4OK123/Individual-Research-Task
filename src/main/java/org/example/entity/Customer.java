package org.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "customers")
@Inheritance(strategy = InheritanceType.JOINED)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "money", nullable = false)
    protected double money;

    // Конструктор за замовчуванням для JPA
    protected Customer() {}

    public Customer(double money) {
        this.money = money;
    }

    // Геттери та сеттери
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    // Віртуальний метод для отримання індивідуальної знижки покупця
    public float getIndividualDiscount() {
        return 0.0f; // Звичайний покупець отримує 0% знижки
    }

    // Метод для покупки товару
    public boolean buyProduct(Product product) {
        if (product == null) {
            System.out.println("Товар не знайдений.");
            return false;
        }

        // Отримуємо знижку покупця
        float customerDiscount = getIndividualDiscount();
        
        // Застосовуємо мінімальну зі знижок (покупця або максимальної для товару)
        float appliedDiscount = Math.min(customerDiscount, product.getMaxDiscountPercentage());
        
        // Обчислюємо фінальну ціну з урахуванням знижки
        double finalPrice = product.getPrice() * (1 - appliedDiscount / 100.0);
        
        // Перевіряємо, чи вистачить грошей
        if (money >= finalPrice) {
            money -= finalPrice;
            System.out.println("Покупка успішна! Товар: " + product.getName() + 
                             ", Ціна: " + product.getPrice() + 
                             ", Знижка: " + appliedDiscount + "%" +
                             ", Підсумкова ціна: " + String.format("%.2f", finalPrice));
            return true;
        } else {
            System.out.println("Недостатньо коштів для покупки товару " + product.getName() + 
                             ". Потрібно: " + String.format("%.2f", finalPrice) + 
                             ", Доступно: " + String.format("%.2f", money));
            return false;
        }
    }
}
