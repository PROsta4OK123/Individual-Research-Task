package org.example.service;

import org.example.entity.Customer;
import org.example.entity.RegularCustomer;
import org.example.repository.CustomerRepository;
import org.example.repository.RegularCustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RegularCustomerRepository regularCustomerRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }

    public List<Customer> getCustomersWithSufficientMoney(double amount) {
        return customerRepository.findCustomersWithSufficientMoney(amount);
    }

    // Методы для работы с постоянными покупателями
    public List<RegularCustomer> getAllRegularCustomers() {
        return regularCustomerRepository.findAll();
    }

    public Optional<RegularCustomer> getRegularCustomerByName(String fullName) {
        return regularCustomerRepository.findByFullName(fullName);
    }

    public RegularCustomer saveRegularCustomer(RegularCustomer customer) {
        return regularCustomerRepository.save(customer);
    }

    public List<RegularCustomer> getTopCustomersByPurchases() {
        return regularCustomerRepository.findTopCustomersByPurchases();
    }
} 