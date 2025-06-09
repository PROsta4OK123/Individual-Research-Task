package org.example.controller;

import org.example.entity.Customer;
import org.example.entity.Product;
import org.example.entity.Purchase;
import org.example.service.CustomerService;
import org.example.service.ProductService;
import org.example.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/purchases")
@CrossOrigin(origins = "*")
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> makePurchase(@RequestParam Long customerId, @RequestParam Long productId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Отримуємо дані до здійснення покупки
            Optional<Customer> customerOpt = customerService.getCustomerById(customerId);
            Optional<Product> productOpt = productService.getProductById(productId);
            
            if (customerOpt.isEmpty() || productOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Покупець або товар не знайдений");
                return ResponseEntity.badRequest().body(response);
            }
            
            Customer customer = customerOpt.get();
            Product product = productOpt.get();
            
            // Розраховуємо ціну та знижку до покупки
            double finalPrice = purchaseService.calculateFinalPrice(customer, product);
            float appliedDiscount = purchaseService.calculateAppliedDiscount(customer, product);
            
            // Здійснюємо покупку
            boolean success = purchaseService.makePurchase(customerId, productId);
            
            if (success) {
                response.put("success", true);
                response.put("message", "Покупка успішно здійснена");
                response.put("productName", product.getName());
                response.put("finalPrice", finalPrice);
                response.put("appliedDiscount", appliedDiscount);
                
                // Отримуємо оновлені дані покупця
                Customer updatedCustomer = customerService.getCustomerById(customerId).get();
                response.put("remainingMoney", updatedCustomer.getMoney());
                
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Покупка не вдалася");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/calculate-price")
    public ResponseEntity<Map<String, Object>> calculatePrice(@RequestParam Long customerId, @RequestParam Long productId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Customer> customer = customerService.getCustomerById(customerId);
            Optional<Product> product = productService.getProductById(productId);
            
            if (customer.isPresent() && product.isPresent()) {
                double finalPrice = purchaseService.calculateFinalPrice(customer.get(), product.get());
                float appliedDiscount = purchaseService.calculateAppliedDiscount(customer.get(), product.get());
                
                response.put("originalPrice", product.get().getPrice());
                response.put("finalPrice", finalPrice);
                response.put("appliedDiscount", appliedDiscount);
                response.put("canAfford", customer.get().getMoney() >= finalPrice);
                
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Покупець або товар не знайдений");
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<Purchase>> getPurchaseHistory(@RequestParam Long customerId) {
        try {
            List<Purchase> history = purchaseService.getPurchaseHistory(customerId);
            return ResponseEntity.ok(history);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getPurchaseStatistics(@RequestParam Long customerId) {
        try {
            Map<String, Object> stats = purchaseService.getPurchaseStatistics(customerId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 