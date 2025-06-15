package org.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "laptops")
public class Laptop extends Product {

    @Column(name = "diagonal_size")
    private short diagonalSize;

    @Column(name = "weight")
    private float weight;

    @Column(name = "cpu_core_count")
    private short cpuCoreCount;

    @Column(name = "memory_count")
    private int memoryCount;

    // Конструктор по умолчанию для JPA
    protected Laptop() {}

    public Laptop(String name, float price, String firm, float maxDiscountPercentage, short diagonalSize, float weight, short cpuCoreCount, int memoryCount) {
        super(name, price, firm, maxDiscountPercentage);
        this.diagonalSize = diagonalSize;
        this.weight = weight;
        this.cpuCoreCount = cpuCoreCount;
        setMemoryCount(memoryCount);
    }

    public Laptop(String name, float price, String firm, float maxDiscountPercentage, short diagonalSize, float weight, short cpuCoreCount, int memoryCount, String imageURL) {
        super(name, price, firm, maxDiscountPercentage, 0, imageURL);
        this.diagonalSize = diagonalSize;
        this.weight = weight;
        this.cpuCoreCount = cpuCoreCount;
        setMemoryCount(memoryCount);
    }

    // Геттеры и сеттеры
    public short getDiagonalSize() {
        return diagonalSize;
    }

    public void setDiagonalSize(short diagonalSize) {
        this.diagonalSize = diagonalSize;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public short getCpuCoreCount() {
        return cpuCoreCount;
    }

    public void setCpuCoreCount(short cpuCoreCount) {
        this.cpuCoreCount = cpuCoreCount;
    }

    public int getMemoryCount() {
        return memoryCount;
    }

    public void setMemoryCount(int memoryCount) {
        if (isPowerOfTwo(memoryCount)) {
            this.memoryCount = memoryCount;
        } else {
            throw new IllegalArgumentException("Количество памяти должно быть степенью 2");
        }
    }

    private boolean isPowerOfTwo(int n) {
        if (n <= 0) {
            return false;
        }
        return (n & (n - 1)) == 0;
    }
}