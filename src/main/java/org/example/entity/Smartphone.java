package org.example.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "smartphones")
public class Smartphone extends MobilePhone {

    @Column(name = "operating_system")
    private String OS;

    @ElementCollection
    @CollectionTable(name = "smartphone_programs", joinColumns = @JoinColumn(name = "smartphone_id"))
    @Column(name = "program_name")
    private List<String> installedPrograms = new ArrayList<>();

    // Конструктор по умолчанию для JPA
    protected Smartphone() {}

    public Smartphone(String name, float price, String firm, float maxDiscountPercentage, boolean isContract, short maxSimValue, String OS, List<String> installedPrograms) {
        super(name, price, firm, maxDiscountPercentage, isContract, maxSimValue);
        this.OS = OS;
        this.installedPrograms = installedPrograms != null ? new ArrayList<>(installedPrograms) : new ArrayList<>();
    }

    public Smartphone(String name, float price, String firm, float maxDiscountPercentage, boolean isContract, short maxSimValue, String OS, List<String> installedPrograms, String imageURL) {
        super(name, price, firm, maxDiscountPercentage, isContract, maxSimValue, imageURL);
        this.OS = OS;
        this.installedPrograms = installedPrograms != null ? new ArrayList<>(installedPrograms) : new ArrayList<>();
    }

    // Геттеры и сеттеры
    public String getOS() {
        return OS;
    }

    public void setOS(String OS) {
        this.OS = OS;
    }

    public List<String> getInstalledPrograms() {
        return installedPrograms;
    }

    public void setInstalledPrograms(List<String> installedPrograms) {
        this.installedPrograms = installedPrograms != null ? new ArrayList<>(installedPrograms) : new ArrayList<>();
    }

    public void addInstalledProgram(String program) {
        this.installedPrograms.add(program);
    }

    public void removeInstalledProgram(String program) {
        this.installedPrograms.remove(program);
    }
}
