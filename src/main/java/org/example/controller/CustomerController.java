package org.example.controller;

import org.example.entity.Customer;
import org.example.entity.RegularCustomer;
import org.example.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        Optional<Customer> customer = customerService.getCustomerById(id);
        return customer.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Customer createCustomer(@RequestBody Customer customer) {
        return customerService.saveCustomer(customer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customerDetails) {
        Optional<Customer> optionalCustomer = customerService.getCustomerById(id);
        if (optionalCustomer.isPresent()) {
            Customer customer = optionalCustomer.get();
            customer.setMoney(customerDetails.getMoney());
            return ResponseEntity.ok(customerService.saveCustomer(customer));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        if (customerService.getCustomerById(id).isPresent()) {
            customerService.deleteCustomer(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/regular")
    public List<RegularCustomer> getAllRegularCustomers() {
        return customerService.getAllRegularCustomers();
    }

    @GetMapping("/regular/name/{name}")
    public ResponseEntity<RegularCustomer> getRegularCustomerByName(@PathVariable String name) {
        Optional<RegularCustomer> customer = customerService.getRegularCustomerByName(name);
        return customer.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/regular")
    public RegularCustomer createRegularCustomer(@RequestBody RegularCustomer customer) {
        return customerService.saveRegularCustomer(customer);
    }

    @GetMapping("/top-customers")
    public List<RegularCustomer> getTopCustomers() {
        return customerService.getTopCustomersByPurchases();
    }

    @GetMapping("/sufficient-money")
    public List<Customer> getCustomersWithSufficientMoney(@RequestParam double amount) {
        return customerService.getCustomersWithSufficientMoney(amount);
    }
} 