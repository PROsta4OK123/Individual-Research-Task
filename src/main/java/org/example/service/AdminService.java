package org.example.service;

import org.example.entity.Admin;
import org.example.entity.Product;
import org.example.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private ProductService productService;

    @Autowired
    private AdminRepository adminRepository;

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
} 