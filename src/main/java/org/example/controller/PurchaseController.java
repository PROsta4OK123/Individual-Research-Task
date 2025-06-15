package org.example.controller;

import org.example.entity.Customer;
import org.example.entity.Product;
import org.example.entity.Purchase;
import org.example.service.CustomerService;
import org.example.service.ProductService;
import org.example.service.PurchaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/purchases")
@CrossOrigin(origins = "*")
public class PurchaseController {

    private static final Logger log = LoggerFactory.getLogger(PurchaseController.class);

    @Autowired
    private PurchaseService purchaseService;
    
    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> makePurchase(
            @RequestParam Long customerId, 
            @RequestParam Long productId) {
        
        log.info("Purchase request: customerId={}, productId={}", customerId, productId);
        
        try {
            // Отримуємо інформацію до покупки
            Optional<Customer> customerOpt = customerService.getCustomerById(customerId);
            Optional<Product> productOpt = productService.getProductById(productId);
            
            if (customerOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Покупець не знайдений"
                ));
            }
            
            if (productOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Товар не знайдений"
                ));
            }
            
            Product product = productOpt.get();
            Customer customerBefore = customerOpt.get();
            
            // Розраховуємо ціну до покупки
            double originalPrice = product.getPrice();
            float customerDiscount = customerBefore.getIndividualDiscount();
            float maxDiscount = product.getMaxDiscountPercentage();
            float appliedDiscount = Math.min(customerDiscount, maxDiscount);
            double finalPrice = originalPrice * (1 - appliedDiscount / 100.0);
            
            boolean success = purchaseService.makePurchase(customerId, productId);
            
            if (success) {
                // Отримуємо оновлену інформацію про покупця
                Customer customerAfter = customerService.getCustomerById(customerId).orElse(customerBefore);
                
                log.info("Purchase successful for customer {} and product {}", customerId, productId);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Покупка успішно завершена!",
                    "productName", product.getName(),
                    "productFirm", product.getFirm(),
                    "originalPrice", originalPrice,
                    "finalPrice", finalPrice,
                    "appliedDiscount", appliedDiscount,
                    "remainingMoney", customerAfter.getMoney()
                ));
            } else {
                log.warn("Purchase failed for customer {} and product {}", customerId, productId);
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Покупка не вдалася"
                ));
            }
        } catch (Exception e) {
            log.error("Error processing purchase: customerId={}, productId={}, error={}", 
                customerId, productId, e.getMessage());
            
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/buy")
    public ResponseEntity<Map<String, Object>> buyProduct(@RequestBody Map<String, Object> request) {
        try {
            Long customerId = Long.valueOf(request.get("customerId").toString());
            Long productId = Long.valueOf(request.get("productId").toString());
            
            log.info("Buy request: customerId={}, productId={}", customerId, productId);
            
            // Перенаправляємо на основний метод
            return makePurchase(customerId, productId);
            
        } catch (Exception e) {
            log.error("Error processing buy request: {}", e.getMessage());
            
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Помилка сервера при обробці покупки: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/history/{customerId}")
    public ResponseEntity<Map<String, Object>> getPurchaseHistory(@PathVariable Long customerId) {
        try {
            log.info("Getting purchase history for customer {}", customerId);
            
            List<Purchase> purchases = purchaseService.getPurchaseHistory(customerId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "purchases", purchases,
                "count", purchases.size()
            ));
        } catch (Exception e) {
            log.error("Error getting purchase history for customer {}: {}", customerId, e.getMessage());
            
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Помилка отримання історії покупок: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/statistics/{customerId}")
    public ResponseEntity<Map<String, Object>> getPurchaseStatistics(@PathVariable Long customerId) {
        try {
            log.info("Getting purchase statistics for customer {}", customerId);
            
            Map<String, Object> stats = purchaseService.getPurchaseStatistics(customerId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "statistics", stats
            ));
        } catch (Exception e) {
            log.error("Error getting purchase statistics for customer {}: {}", customerId, e.getMessage());
            
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Помилка отримання статистики покупок: " + e.getMessage()
            ));
        }
    }
} 