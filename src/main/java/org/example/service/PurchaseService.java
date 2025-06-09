package org.example.service;

import org.example.entity.Customer;
import org.example.entity.Product;
import org.example.entity.RegularCustomer;
import org.example.entity.Purchase;
import org.example.repository.PurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
public class PurchaseService {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ProductService productService;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Transactional
    public boolean makePurchase(Long customerId, Long productId) {
        Customer customer = customerService.getCustomerById(customerId)
                .orElseThrow(() -> new RuntimeException("Покупець не знайдений"));
        
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new RuntimeException("Товар не знайдений"));

        return processPurchase(customer, product);
    }

    private boolean processPurchase(Customer customer, Product product) {
        if (product == null) {
            return false;
        }

        // Перевіряємо, чи є товар на складі
        if (product.getQuantity() <= 0) {
            throw new RuntimeException("Товару немає на складі");
        }

        // Отримуємо знижку покупця
        float customerDiscount = customer.getIndividualDiscount();
        
        // Застосовуємо мінімальну зі знижок (покупця або максимальної для товару)
        float appliedDiscount = Math.min(customerDiscount, product.getMaxDiscountPercentage());
        
        // Обчислюємо фінальну ціну з урахуванням знижки
        double originalPrice = product.getPrice();
        double finalPrice = originalPrice * (1 - appliedDiscount / 100.0);
        
        // Перевіряємо, чи вистачить грошей
        if (customer.getMoney() >= finalPrice) {
            customer.setMoney(customer.getMoney() - finalPrice);
            
            // Зменшуємо кількість товару на складі
            product.setQuantity(product.getQuantity() - 1);
            productService.saveProduct(product);
            
            // Для постійного покупця збільшуємо загальну суму покупок
            if (customer instanceof RegularCustomer) {
                RegularCustomer regularCustomer = (RegularCustomer) customer;
                regularCustomer.setTotalPurchasesAmount(
                    regularCustomer.getTotalPurchasesAmount() + (float) finalPrice
                );
            }
            
            // Зберігаємо покупку в історію
            Purchase purchase = new Purchase(
                customer.getId(),
                product.getId(),
                product.getName(),
                product.getFirm(),
                originalPrice,
                finalPrice,
                appliedDiscount,
                product.getImageURL()
            );
            purchaseRepository.save(purchase);
            
            // Зберігаємо зміни
            customerService.saveCustomer(customer);
            
            return true;
        } else {
            throw new RuntimeException("Недостатньо коштів для покупки");
        }
    }

    public double calculateFinalPrice(Customer customer, Product product) {
        float customerDiscount = customer.getIndividualDiscount();
        float appliedDiscount = Math.min(customerDiscount, product.getMaxDiscountPercentage());
        return product.getPrice() * (1 - appliedDiscount / 100.0);
    }

    public float calculateAppliedDiscount(Customer customer, Product product) {
        float customerDiscount = customer.getIndividualDiscount();
        return Math.min(customerDiscount, product.getMaxDiscountPercentage());
    }

    // Методи для роботи з історією покупок
    public List<Purchase> getPurchaseHistory(Long customerId) {
        return purchaseRepository.findByCustomerIdOrderByPurchaseDateDesc(customerId);
    }

    public List<Purchase> getPurchaseHistoryByDateRange(Long customerId, LocalDateTime startDate, LocalDateTime endDate) {
        return purchaseRepository.findByCustomerIdAndDateRange(customerId, startDate, endDate);
    }

    public Map<String, Object> getPurchaseStatistics(Long customerId) {
        Map<String, Object> stats = new HashMap<>();
        
        Double totalSpent = purchaseRepository.getTotalSpentByCustomer(customerId);
        long purchaseCount = purchaseRepository.countByCustomerId(customerId);
        List<Object[]> topProducts = purchaseRepository.getTopProductsByCustomer(customerId);
        
        stats.put("totalSpent", totalSpent != null ? totalSpent : 0.0);
        stats.put("purchaseCount", purchaseCount);
        stats.put("averagePurchase", purchaseCount > 0 ? (totalSpent != null ? totalSpent / purchaseCount : 0.0) : 0.0);
        stats.put("topProducts", topProducts);
        
        return stats;
    }
} 