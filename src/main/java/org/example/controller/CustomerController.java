package org.example.controller;

import org.example.entity.Customer;
import org.example.entity.RegularCustomer;
import org.example.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);
    @Autowired
    private CustomerService customerService;

    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        Optional<Customer> customer = customerService.getCustomerById(id);
        log.info("Getting customer with id " + id);
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
            log.info("Updating customer with id " + id);
            return ResponseEntity.ok(customerService.saveCustomer(customer));
        }
        log.info("Customer with id " + id + " not found");
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        if (customerService.getCustomerById(id).isPresent()) {
            customerService.deleteCustomer(id);
            log.info("Deleting customer with id " + id);
            return ResponseEntity.ok().build();
        }
        log.info("Customer with id " + id + " not found");
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/regular")
    public List<RegularCustomer> getAllRegularCustomers() {
        log.info("Getting all regular customers");
        return customerService.getAllRegularCustomers();
    }

    @GetMapping("/regular/name/{name}")
    public ResponseEntity<RegularCustomer> getRegularCustomerByName(@PathVariable String name) {
        Optional<RegularCustomer> customer = customerService.getRegularCustomerByName(name);
        log.info("Getting regular customer with name " + name);
        return customer.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/regular")
    public RegularCustomer createRegularCustomer(@RequestBody RegularCustomer customer) {
        log.info("Creating regular customer with name " + customer.getFullName());
        return customerService.saveRegularCustomer(customer);
    }

    @GetMapping("/top-customers")
    public List<RegularCustomer> getTopCustomers() {
        log.info("Getting top customers by purchases");
        return customerService.getTopCustomersByPurchases();
    }

    @GetMapping("/sufficient-money")
    public List<Customer> getCustomersWithSufficientMoney(@RequestParam double amount) {
        log.info("Getting customers with sufficient money " + amount);
        return customerService.getCustomersWithSufficientMoney(amount);
    }
} 