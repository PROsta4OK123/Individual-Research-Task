package org.example.repository;

import org.example.entity.RegularCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegularCustomerRepository extends JpaRepository<RegularCustomer, Long> {
    
    Optional<RegularCustomer> findByFullName(String fullName);
    
    List<RegularCustomer> findByTotalPurchasesAmountGreaterThan(float amount);
    
    @Query("SELECT rc FROM RegularCustomer rc ORDER BY rc.totalPurchasesAmount DESC")
    List<RegularCustomer> findTopCustomersByPurchases();
} 