package org.example.controller;

import org.example.entity.Customer;
import org.example.entity.Product;
import org.example.service.CustomerService;
import org.example.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    @Autowired
    private ProductService productService;
    @Autowired
    private CustomerService customerService;

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return productService.saveProduct(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        Optional<Product> optionalProduct = productService.getProductById(id);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setName(productDetails.getName());
            product.setPrice(productDetails.getPrice());
            product.setFirm(productDetails.getFirm());
            product.setMaxDiscountPercentage(productDetails.getMaxDiscountPercentage());
            log.info("Updating product with id " + id);
            return ResponseEntity.ok(productService.saveProduct(product));
        }
        log.info("Product with id " + id + " not found when tries to update");
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productService.getProductById(id).isPresent()) {
            productService.deleteProduct(id);
            log.info("Deleting product with id " + id);
            return ResponseEntity.ok().build();
        }
        log.info("Product with id " + id + " not found when tries to delete");
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/firm/{firm}")
    public List<Product> getProductsByFirm(@PathVariable String firm) {
        return productService.getProductsByFirm(firm);
    }

    @GetMapping("/price-range")
    public List<Product> getProductsByPriceRange(@RequestParam float minPrice, @RequestParam float maxPrice) {
        return productService.getProductsByPriceRange(minPrice, maxPrice);
    }

    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String name) {
        return productService.searchProductsByName(name);
    }

    @GetMapping("/calculate-price")
    public ResponseEntity<Map<String, Object>> calculatePrice(
            @RequestParam Long customerId,
            @RequestParam Long productId) {
        
        try {
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
            
            Customer customer = customerOpt.get();
            Product product = productOpt.get();
            
            double originalPrice = product.getPrice();
            float customerDiscount = customer.getIndividualDiscount();
            float maxDiscount = product.getMaxDiscountPercentage();
            float appliedDiscount = Math.min(customerDiscount, maxDiscount);
            double finalPrice = originalPrice * (1 - appliedDiscount / 100.0);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "originalPrice", originalPrice,
                "customerDiscount", customerDiscount,
                "maxProductDiscount", maxDiscount,
                "appliedDiscount", appliedDiscount,
                "finalPrice", finalPrice,
                "savings", originalPrice - finalPrice
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Помилка розрахунку ціни: " + e.getMessage()
            ));
        }
    }
} 