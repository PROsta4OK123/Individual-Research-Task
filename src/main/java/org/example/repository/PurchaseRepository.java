package org.example.repository;

import org.example.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    
    // Знайти всі покупки клієнта, відсортовані за датою (новіші спочатку)
    List<Purchase> findByCustomerIdOrderByPurchaseDateDesc(Long customerId);
    
    // Знайти покупки клієнта за певний період
    @Query("SELECT p FROM Purchase p WHERE p.customerId = :customerId AND p.purchaseDate BETWEEN :startDate AND :endDate ORDER BY p.purchaseDate DESC")
    List<Purchase> findByCustomerIdAndDateRange(@Param("customerId") Long customerId, 
                                               @Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);
    
    // Підрахувати загальну суму витрат клієнта
    @Query("SELECT SUM(p.finalPrice) FROM Purchase p WHERE p.customerId = :customerId")
    Double getTotalSpentByCustomer(@Param("customerId") Long customerId);
    
    // Підрахувати кількість покупок клієнта
    long countByCustomerId(Long customerId);
    
    // Знайти топ товарів клієнта
    @Query("SELECT p.productName, COUNT(p) as purchaseCount FROM Purchase p WHERE p.customerId = :customerId GROUP BY p.productName ORDER BY purchaseCount DESC")
    List<Object[]> getTopProductsByCustomer(@Param("customerId") Long customerId);
} 