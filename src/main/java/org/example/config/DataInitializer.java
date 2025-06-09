package org.example.config;

import org.example.entity.*;
import org.example.service.CustomerService;
import org.example.service.ProductService;
import org.example.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AuthService authService;

    @Override
    public void run(String... args) throws Exception {
        // Инициализируем продукты, если их еще нет
        if (productService.getAllProducts().isEmpty()) {
            initializeProducts();
        }

        // Инициализируем покупателей, если их еще нет
        if (customerService.getAllCustomers().isEmpty()) {
            initializeCustomers();
        }

        // Создаем тестовых пользователей и админа
        initializeUsers();
    }

    private void initializeProducts() {
        // Создаем ноутбуки
        Laptop laptop1 = new Laptop("Ноутбук Acer Aspire 5", 1500.0f, "Acer", 10.0f, (short) 15, 2.1f, (short) 4, 8192);
        laptop1.setQuantity(25);
        Laptop laptop2 = new Laptop("Ноутбук HP Pavilion", 1200.0f, "HP", 12.0f, (short) 14, 1.8f, (short) 6, 16384);
        laptop2.setQuantity(15);

        // Создаем мобильные телефоны
        MobilePhone phone1 = new MobilePhone("Nokia 3310", 100.0f, "Nokia", 5.0f, false, (short) 1);
        phone1.setQuantity(50);
        MobilePhone phone2 = new MobilePhone("iPhone 13 Pro", 1000.0f, "Apple", 8.0f, true, (short) 2);
        phone2.setQuantity(10);

        // Создаем смартфоны
        Smartphone smartphone1 = new Smartphone("Samsung Galaxy S21", 800.0f, "Samsung", 15.0f, true, (short) 2, "Android", 
                Arrays.asList("WhatsApp", "Telegram", "Instagram"));
        smartphone1.setQuantity(30);
        Smartphone smartphone2 = new Smartphone("iPhone 14", 1200.0f, "Apple", 10.0f, true, (short) 2, "iOS", 
                Arrays.asList("Safari", "FaceTime", "iMessage"));
        smartphone2.setQuantity(20);

        // Сохраняем продукты
        productService.saveProduct(laptop1);
        productService.saveProduct(laptop2);
        productService.saveProduct(phone1);
        productService.saveProduct(phone2);
        productService.saveProduct(smartphone1);
        productService.saveProduct(smartphone2);

        System.out.println("Тестовые продукты созданы успешно!");
    }

    private void initializeCustomers() {
        // Создаем обычных покупателей
        Customer customer1 = new Customer(2000.0);
        Customer customer2 = new Customer(5000.0);
        Customer customer3 = new Customer(1000.0);

        // Создаем постоянных покупателей
        RegularCustomer regularCustomer1 = new RegularCustomer(3000.0, "Иванов Иван Иванович", 5000.0f);
        RegularCustomer regularCustomer2 = new RegularCustomer(10000.0, "Петрова Анна Сергеевна", 15000.0f);
        RegularCustomer regularCustomer3 = new RegularCustomer(7500.0, "Сидоров Петр Александрович", 8000.0f);

        // Сохраняем покупателей
        customerService.saveCustomer(customer1);
        customerService.saveCustomer(customer2);
        customerService.saveCustomer(customer3);
        customerService.saveRegularCustomer(regularCustomer1);
        customerService.saveRegularCustomer(regularCustomer2);
        customerService.saveRegularCustomer(regularCustomer3);

        System.out.println("Тестовые покупатели созданы успешно!");
    }

    private void initializeUsers() {
        // Проверяем, существуют ли уже тестовые пользователи
        try {
            // Создаем тестового пользователя только если его еще нет
            if (!authService.userExists("testuser")) {
                authService.registerUser("testuser", "user@test.com", "password123");
                System.out.println("✅ Тестовый пользователь создан: testuser / password123");
            }
            
            // Создаем тестового администратора только если его еще нет
            if (!authService.userExists("admin")) {
                authService.registerAdmin("admin", "admin@test.com", "admin123", "ADMIN_SECRET_KEY_2024");
                System.out.println("✅ Тестовый админ создан: admin / admin123");
            }

            System.out.println("\n🎯 Информация о системе:");
            System.out.println("📝 Зарегистрированные пользователи автоматически становятся постоянными покупателями");
            System.out.println("⭐ Постоянные покупатели получают персональные скидки до 15%");
            System.out.println("🎫 Гости получают фиксированный баланс без скидок");
            System.out.println("👨‍💼 Администраторы получают повышенный баланс и права управления");
            System.out.println("\n🔑 Админ ключ: ADMIN_SECRET_KEY_2024");
        } catch (Exception e) {
            System.out.println("ℹ️ Тестовые пользователи уже существуют или произошла ошибка: " + e.getMessage());
        }
    }
} 