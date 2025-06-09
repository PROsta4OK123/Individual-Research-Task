package org.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admins")
public class Admin extends User {

    @Column(name = "admin_level")
    private int adminLevel = 1;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "permissions")
    private String permissions = "MANAGE_PRODUCTS,MANAGE_USERS";

    // Конструктор по умолчанию для JPA
    protected Admin() {
        super();
    }

    public Admin(String username, String email, String password) {
        super(username, email, password, Role.ADMIN);
        this.adminLevel = 1;
        this.permissions = "MANAGE_PRODUCTS,MANAGE_USERS";
    }

    public Admin(String username, String email, String password, int adminLevel, String permissions) {
        super(username, email, password, Role.ADMIN);
        this.adminLevel = adminLevel;
        this.permissions = permissions;
    }

    // Геттеры и сеттеры
    public int getAdminLevel() {
        return adminLevel;
    }

    public void setAdminLevel(int adminLevel) {
        this.adminLevel = adminLevel;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }
} 