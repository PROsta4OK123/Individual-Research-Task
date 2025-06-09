package org.example.service;

import org.example.entity.Admin;
import org.example.entity.Product;
import org.example.entity.User;
import org.example.entity.Customer;
import org.example.entity.RegularCustomer;
import org.example.repository.AdminRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private ProductService productService;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public Map<String, Object> updateProductQuantity(Long adminId, Long productId, int newQuantity) {
        Map<String, Object> response = new HashMap<>();

        // Перевіряємо права адміністратора
        Optional<Admin> adminOpt = adminRepository.findById(adminId);
        if (adminOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Адміністратор не знайдений");
            return response;
        }

        Admin admin = adminOpt.get();
        if (!admin.hasPermission("MANAGE_PRODUCTS")) {
            response.put("success", false);
            response.put("message", "Немає прав на управління товарами");
            return response;
        }

        // Оновлюємо кількість товару
        Optional<Product> productOpt = productService.getProductById(productId);
        if (productOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Товар не знайдений");
            return response;
        }

        Product product = productOpt.get();
        int oldQuantity = product.getQuantity();
        product.setQuantity(newQuantity);
        productService.saveProduct(product);

        response.put("success", true);
        response.put("message", "Кількість товару оновлено");
        response.put("product", product);
        response.put("oldQuantity", oldQuantity);
        response.put("newQuantity", newQuantity);

        return response;
    }

    @Transactional
    public Map<String, Object> deleteProduct(Long adminId, Long productId) {
        Map<String, Object> response = new HashMap<>();

        // Перевіряємо права адміністратора
        Optional<Admin> adminOpt = adminRepository.findById(adminId);
        if (adminOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Адміністратор не знайдений");
            return response;
        }

        Admin admin = adminOpt.get();
        if (!admin.hasPermission("MANAGE_PRODUCTS")) {
            response.put("success", false);
            response.put("message", "Немає прав на управління товарами");
            return response;
        }

        // Перевіряємо існування товару
        Optional<Product> productOpt = productService.getProductById(productId);
        if (productOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Товар не знайдений");
            return response;
        }

        Product product = productOpt.get();
        productService.deleteProduct(productId);

        response.put("success", true);
        response.put("message", "Товар успішно видалено");
        response.put("deletedProduct", Map.of(
            "id", product.getId(),
            "name", product.getName(),
            "price", product.getPrice()
        ));

        return response;
    }

    @Transactional
    public Map<String, Object> createProduct(Long adminId, Product product) {
        Map<String, Object> response = new HashMap<>();

        // Перевіряємо права адміністратора
        Optional<Admin> adminOpt = adminRepository.findById(adminId);
        if (adminOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Адміністратор не знайдений");
            return response;
        }

        Admin admin = adminOpt.get();
        if (!admin.hasPermission("MANAGE_PRODUCTS")) {
            response.put("success", false);
            response.put("message", "Немає прав на управління товарами");
            return response;
        }

        // Створюємо новий товар
        Product savedProduct = productService.saveProduct(product);

        response.put("success", true);
        response.put("message", "Товар успішно створено");
        response.put("product", savedProduct);

        return response;
    }

    @Transactional
    public Map<String, Object> updateProduct(Long adminId, Long productId, Map<String, Object> updates) {
        Map<String, Object> response = new HashMap<>();

        // Перевіряємо права адміністратора
        Optional<Admin> adminOpt = adminRepository.findById(adminId);
        if (adminOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Адміністратор не знайдений");
            return response;
        }

        Admin admin = adminOpt.get();
        if (!admin.hasPermission("MANAGE_PRODUCTS")) {
            response.put("success", false);
            response.put("message", "Немає прав на управління товарами");
            return response;
        }

        // Перевіряємо існування товару
        Optional<Product> productOpt = productService.getProductById(productId);
        if (productOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Товар не знайдений");
            return response;
        }

        Product product = productOpt.get();
        
        // Створюємо копію старих даних для логування
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("name", product.getName());
        oldValues.put("price", product.getPrice());
        oldValues.put("firm", product.getFirm());
        oldValues.put("maxDiscountPercentage", product.getMaxDiscountPercentage());
        
        try {
            // Оновлюємо загальні поля
            if (updates.containsKey("name")) {
                product.setName((String) updates.get("name"));
            }
            if (updates.containsKey("price")) {
                product.setPrice(((Number) updates.get("price")).floatValue());
            }
            if (updates.containsKey("firm")) {
                product.setFirm((String) updates.get("firm"));
            }
            if (updates.containsKey("maxDiscountPercentage")) {
                product.setMaxDiscountPercentage(((Number) updates.get("maxDiscountPercentage")).floatValue());
            }
            if (updates.containsKey("imageURL")) {
                product.setImageURL((String) updates.get("imageURL"));
            }
            if (updates.containsKey("quantity")) {
                product.setQuantity(((Number) updates.get("quantity")).intValue());
            }

            // Оновлюємо специфічні поля в залежності від типу товару
            String productType = product.getClass().getSimpleName();
            switch (productType) {
                case "Laptop":
                    updateLaptopSpecificFields(product, updates, oldValues);
                    break;
                case "MobilePhone":
                    updateMobilePhoneSpecificFields(product, updates, oldValues);
                    break;
                case "Smartphone":
                    updateSmartphoneSpecificFields(product, updates, oldValues);
                    break;
            }

            // Зберігаємо оновлений товар
            Product savedProduct = productService.saveProduct(product);

            response.put("success", true);
            response.put("message", "Товар успішно оновлено");
            response.put("product", savedProduct);
            response.put("productType", productType);
            response.put("oldValues", oldValues);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Помилка при оновленні товару: " + e.getMessage());
        }

        return response;
    }

    private void updateLaptopSpecificFields(Product product, Map<String, Object> updates, Map<String, Object> oldValues) {
        if (product instanceof org.example.entity.Laptop) {
            org.example.entity.Laptop laptop = (org.example.entity.Laptop) product;
            
            oldValues.put("diagonalSize", laptop.getDiagonalSize());
            oldValues.put("weight", laptop.getWeight());
            oldValues.put("cpuCoreCount", laptop.getCpuCoreCount());
            oldValues.put("memoryCount", laptop.getMemoryCount());
            
            if (updates.containsKey("diagonalSize")) {
                laptop.setDiagonalSize(((Number) updates.get("diagonalSize")).shortValue());
            }
            if (updates.containsKey("weight")) {
                laptop.setWeight(((Number) updates.get("weight")).floatValue());
            }
            if (updates.containsKey("cpuCoreCount")) {
                laptop.setCpuCoreCount(((Number) updates.get("cpuCoreCount")).shortValue());
            }
            if (updates.containsKey("memoryCount")) {
                laptop.setMemoryCount(((Number) updates.get("memoryCount")).intValue());
            }
        }
    }

    private void updateMobilePhoneSpecificFields(Product product, Map<String, Object> updates, Map<String, Object> oldValues) {
        if (product instanceof org.example.entity.MobilePhone) {
            org.example.entity.MobilePhone phone = (org.example.entity.MobilePhone) product;
            
            oldValues.put("isContract", phone.isContract());
            oldValues.put("maxSimValue", phone.getMaxSimValue());
            
            if (updates.containsKey("isContract")) {
                phone.setContract((Boolean) updates.get("isContract"));
            }
            if (updates.containsKey("maxSimValue")) {
                phone.setMaxSimValue(((Number) updates.get("maxSimValue")).shortValue());
            }
        }
    }

    private void updateSmartphoneSpecificFields(Product product, Map<String, Object> updates, Map<String, Object> oldValues) {
        if (product instanceof org.example.entity.Smartphone) {
            org.example.entity.Smartphone smartphone = (org.example.entity.Smartphone) product;
            
            oldValues.put("OS", smartphone.getOS());
            oldValues.put("installedPrograms", smartphone.getInstalledPrograms());
            oldValues.put("isContract", smartphone.isContract());
            oldValues.put("maxSimValue", smartphone.getMaxSimValue());
            
            if (updates.containsKey("OS")) {
                smartphone.setOS((String) updates.get("OS"));
            }
            if (updates.containsKey("installedPrograms")) {
                // Конвертируем строку в список программ
                String programsStr = (String) updates.get("installedPrograms");
                if (programsStr != null && !programsStr.trim().isEmpty()) {
                    List<String> programs = Arrays.asList(programsStr.split(","));
                    // Обрезаем пробелы
                    programs = programs.stream().map(String::trim).collect(Collectors.toList());
                    smartphone.setInstalledPrograms(programs);
                } else {
                    smartphone.setInstalledPrograms(new ArrayList<>());
                }
            }
            if (updates.containsKey("isContract")) {
                smartphone.setContract((Boolean) updates.get("isContract"));
            }
            if (updates.containsKey("maxSimValue")) {
                smartphone.setMaxSimValue(((Number) updates.get("maxSimValue")).shortValue());
            }
        }
    }

    public Map<String, Object> getProductStats() {
        Map<String, Object> stats = new HashMap<>();
        
        var products = productService.getAllProducts();
        stats.put("totalProducts", products.size());
        stats.put("totalQuantity", products.stream().mapToInt(Product::getQuantity).sum());
        stats.put("averagePrice", products.stream().mapToDouble(Product::getPrice).average().orElse(0.0));
        stats.put("outOfStock", products.stream().filter(p -> p.getQuantity() == 0).count());
        stats.put("lowStock", products.stream().filter(p -> p.getQuantity() > 0 && p.getQuantity() < 10).count());

        return stats;
    }

    // === УПРАВЛІННЯ КОРИСТУВАЧАМИ ===

    public Map<String, Object> getAllUsers(Long adminId) {
        Map<String, Object> response = new HashMap<>();

        // Перевіряємо права адміністратора
        Optional<Admin> adminOpt = adminRepository.findById(adminId);
        if (adminOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Адміністратор не знайдений");
            return response;
        }

        Admin admin = adminOpt.get();
        if (!admin.hasPermission("MANAGE_USERS")) {
            response.put("success", false);
            response.put("message", "Немає прав на управління користувачами");
            return response;
        }

        try {
            List<User> users = userRepository.findAll();
            List<Map<String, Object>> userList = new ArrayList<>();

            for (User user : users) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("email", user.getEmail());
                userInfo.put("role", user.getRole().toString());
                userInfo.put("isActive", user.isActive());
                userInfo.put("createdAt", user.getCreatedAt());
                
                // Шукаємо відповідного покупця
                RegularCustomer customer = findCustomerByUserId(user.getId());
                if (customer != null) {
                    userInfo.put("customerId", customer.getId());
                    userInfo.put("balance", customer.getMoney());
                    userInfo.put("discount", customer.getIndividualDiscount());
                    userInfo.put("totalPurchases", customer.getTotalPurchasesAmount());
                    userInfo.put("fullName", customer.getFullName());
                }
                
                userList.add(userInfo);
            }

            response.put("success", true);
            response.put("users", userList);
            response.put("totalUsers", users.size());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Помилка отримання користувачів: " + e.getMessage());
        }

        return response;
    }

    public Map<String, Object> getUserDetails(Long adminId, Long userId) {
        Map<String, Object> response = new HashMap<>();

        // Перевіряємо права адміністратора
        Optional<Admin> adminOpt = adminRepository.findById(adminId);
        if (adminOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Адміністратор не знайдений");
            return response;
        }

        Admin admin = adminOpt.get();
        if (!admin.hasPermission("MANAGE_USERS")) {
            response.put("success", false);
            response.put("message", "Немає прав на управління користувачами");
            return response;
        }

        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Користувач не знайдений");
                return response;
            }

            User user = userOpt.get();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("role", user.getRole().toString());
            userInfo.put("isActive", user.isActive());
            userInfo.put("createdAt", user.getCreatedAt());

            // Шукаємо відповідного покупця
            RegularCustomer customer = findCustomerByUserId(user.getId());
            if (customer != null) {
                userInfo.put("customerId", customer.getId());
                userInfo.put("balance", customer.getMoney());
                userInfo.put("discount", customer.getIndividualDiscount());
                userInfo.put("totalPurchases", customer.getTotalPurchasesAmount());
                userInfo.put("fullName", customer.getFullName());
            }

            response.put("success", true);
            response.put("user", userInfo);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Помилка отримання деталей користувача: " + e.getMessage());
        }

        return response;
    }

    @Transactional
    public Map<String, Object> updateUser(Long adminId, Long userId, Map<String, Object> updates) {
        Map<String, Object> response = new HashMap<>();

        // Перевіряємо права адміністратора
        Optional<Admin> adminOpt = adminRepository.findById(adminId);
        if (adminOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Адміністратор не знайдений");
            return response;
        }

        Admin admin = adminOpt.get();
        if (!admin.hasPermission("MANAGE_USERS")) {
            response.put("success", false);
            response.put("message", "Немає прав на управління користувачами");
            return response;
        }

        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Користувач не знайдений");
                return response;
            }

            User user = userOpt.get();
            
            // Оновлюємо дані користувача
            if (updates.containsKey("role")) {
                String roleStr = (String) updates.get("role");
                user.setRole(User.Role.valueOf(roleStr));
            }
            if (updates.containsKey("isActive")) {
                user.setActive((Boolean) updates.get("isActive"));
            }

            userRepository.save(user);

            // Оновлюємо дані покупця, якщо є
            RegularCustomer customer = findCustomerByUserId(user.getId());
            if (customer != null) {
                if (updates.containsKey("balance")) {
                    double balance = ((Number) updates.get("balance")).doubleValue();
                    customer.setMoney(balance);
                }
                if (updates.containsKey("totalPurchases")) {
                    float totalPurchases = ((Number) updates.get("totalPurchases")).floatValue();
                    customer.setTotalPurchasesAmount(totalPurchases);
                }
                if (updates.containsKey("fullName")) {
                    customer.setFullName((String) updates.get("fullName"));
                }
                
                customerService.saveRegularCustomer(customer);
            }

            response.put("success", true);
            response.put("message", "Дані користувача успішно оновлено");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Помилка оновлення користувача: " + e.getMessage());
        }

        return response;
    }

    @Transactional
    public Map<String, Object> resetUserPassword(Long adminId, Long userId) {
        Map<String, Object> response = new HashMap<>();

        // Перевіряємо права адміністратора
        Optional<Admin> adminOpt = adminRepository.findById(adminId);
        if (adminOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Адміністратор не знайдений");
            return response;
        }

        Admin admin = adminOpt.get();
        if (!admin.hasPermission("MANAGE_USERS")) {
            response.put("success", false);
            response.put("message", "Немає прав на управління користувачами");
            return response;
        }

        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Користувач не знайдений");
                return response;
            }

            User user = userOpt.get();
            String newPassword = "password123"; // Стандартний пароль
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            response.put("success", true);
            response.put("message", "Пароль користувача скинуто");
            response.put("newPassword", newPassword);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Помилка скидання пароля: " + e.getMessage());
        }

        return response;
    }

    public Map<String, Object> getUserStats(Long adminId) {
        Map<String, Object> response = new HashMap<>();

        // Перевіряємо права адміністратора
        Optional<Admin> adminOpt = adminRepository.findById(adminId);
        if (adminOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Адміністратор не знайдений");
            return response;
        }

        Admin admin = adminOpt.get();
        if (!admin.hasPermission("MANAGE_USERS")) {
            response.put("success", false);
            response.put("message", "Немає прав на управління користувачами");
            return response;
        }

        try {
            List<User> users = userRepository.findAll();
            List<RegularCustomer> customers = customerService.getAllRegularCustomers();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", users.size());
            stats.put("activeUsers", users.stream().filter(User::isActive).count());
            stats.put("adminUsers", users.stream().filter(u -> u.getRole() == User.Role.ADMIN).count());
            stats.put("regularUsers", users.stream().filter(u -> u.getRole() == User.Role.USER).count());
            
            if (!customers.isEmpty()) {
                stats.put("totalBalance", customers.stream().mapToDouble(Customer::getMoney).sum());
                stats.put("averageBalance", customers.stream().mapToDouble(Customer::getMoney).average().orElse(0.0));
                stats.put("totalPurchases", customers.stream().mapToDouble(RegularCustomer::getTotalPurchasesAmount).sum());
                stats.put("averageDiscount", customers.stream().mapToDouble(Customer::getIndividualDiscount).average().orElse(0.0));
            }
            
            response.put("success", true);
            response.put("stats", stats);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Помилка отримання статистики: " + e.getMessage());
        }

        return response;
    }

    private RegularCustomer findCustomerByUserId(Long userId) {
        // Шукаємо покупця за ім'ям (містить ID користувача)
        String searchPattern = "(ID: " + userId + ")";
        return customerService.getAllRegularCustomers().stream()
                .filter(customer -> customer.getFullName().contains(searchPattern))
                .findFirst()
                .orElse(null);
    }
} 