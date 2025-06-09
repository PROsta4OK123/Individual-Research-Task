package org.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "regular_customers")
public class RegularCustomer extends Customer {

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "total_purchases_amount")
    private float totalPurchasesAmount;

    // Конструктор за замовчуванням для JPA
    protected RegularCustomer() {}

    public RegularCustomer(double money, String fullName, float totalPurchasesAmount) {
        super(money);
        this.fullName = fullName;
        this.totalPurchasesAmount = totalPurchasesAmount;
    }

    // Геттери та сеттери
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public float getTotalPurchasesAmount() {
        return totalPurchasesAmount;
    }

    public void setTotalPurchasesAmount(float totalPurchasesAmount) {
        this.totalPurchasesAmount = totalPurchasesAmount;
    }

    // Перевизначаємо метод отримання індивідуальної знижки
    @Override
    public float getIndividualDiscount() {
        // Знижка = загальна вартість покупок / 1000, але не більше 15%
        float discount = totalPurchasesAmount / 1000.0f;
        return Math.min(discount, 15.0f);
    }

    // Перевизначаємо метод покупки товару для врахування суми покупок
    @Override
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
            // Збільшуємо загальну вартість покупок для постійного покупця
            totalPurchasesAmount += (float) finalPrice;
            
            System.out.println("Покупка успішна! Постійний покупець: " + fullName +
                             ", Товар: " + product.getName() + 
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
