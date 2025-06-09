package org.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "mobile_phones")
public class MobilePhone extends Product {

    @Column(name = "is_contract")
    private boolean isContract = false;

    @Column(name = "max_sim_value")
    private short maxSimValue = 1;

    // Конструктор по умолчанию для JPA
    protected MobilePhone() {}

    public MobilePhone(String name, float price, String firm, float maxDiscountPercentage, boolean isContract, short maxSimValue) {
        super(name, price, firm, maxDiscountPercentage);
        this.isContract = isContract;
        if(maxSimValue <= 0) {
            throw new IllegalArgumentException("Max SIM value must be positive");
        }
        this.maxSimValue = maxSimValue;
    }

    public MobilePhone(String name, float price, String firm, float maxDiscountPercentage, boolean isContract, short maxSimValue, String imageURL) {
        super(name, price, firm, maxDiscountPercentage, 0, imageURL);
        this.isContract = isContract;
        if(maxSimValue <= 0) {
            throw new IllegalArgumentException("Max SIM value must be positive");
        }
        this.maxSimValue = maxSimValue;
    }

    // Геттеры и сеттеры
    public boolean isContract() {
        return isContract;
    }

    public void setContract(boolean contract) {
        isContract = contract;
    }

    public short getMaxSimValue() {
        return maxSimValue;
    }

    public void setMaxSimValue(short maxSimValue) {
        if(maxSimValue <= 0) {
            throw new IllegalArgumentException("Max SIM value must be positive");
        }
        this.maxSimValue = maxSimValue;
    }
}
