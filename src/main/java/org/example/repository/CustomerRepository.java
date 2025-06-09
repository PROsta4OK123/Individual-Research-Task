package org.example.repository;

import org.example.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    List<Customer> findByMoneyGreaterThan(double money);
    
    @Query("SELECT c FROM Customer c WHERE c.money >= ?1")
    List<Customer> findCustomersWithSufficientMoney(double amount);
} 