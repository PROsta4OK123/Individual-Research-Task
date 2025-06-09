package org.example.controller;

import org.example.entity.Laptop;
import org.example.entity.MobilePhone;
import org.example.entity.Product;
import org.example.entity.Smartphone;
import org.example.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PutMapping("/products/{productId}/quantity")
    public ResponseEntity<Map<String, Object>> updateProductQuantity(
            @PathVariable Long productId,
            @RequestParam Long adminId,
            @RequestParam int quantity) {

        Map<String, Object> response = adminService.updateProductQuantity(adminId, productId, quantity);

        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Map<String, Object>> deleteProduct(
            @PathVariable Long productId,
            @RequestParam Long adminId) {

        Map<String, Object> response = adminService.deleteProduct(adminId, productId);

        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/products/laptop")
    public ResponseEntity<Map<String, Object>> createLaptop(
            @RequestParam Long adminId,
            @RequestBody Map<String, Object> laptopData) {

        try {
            Laptop laptop = new Laptop(
                (String) laptopData.get("name"),
                Float.parseFloat(laptopData.get("price").toString()),
                (String) laptopData.get("firm"),
                Float.parseFloat(laptopData.get("maxDiscountPercentage").toString()),
                Short.parseShort(laptopData.get("diagonalSize").toString()),
                Float.parseFloat(laptopData.get("weight").toString()),
                Short.parseShort(laptopData.get("cpuCoreCount").toString()),
                Integer.parseInt(laptopData.get("memoryCount").toString()),
                (String) laptopData.get("imageURL")
            );
            
            if (laptopData.containsKey("quantity")) {
                laptop.setQuantity(Integer.parseInt(laptopData.get("quantity").toString()));
            }

            Map<String, Object> response = adminService.createProduct(adminId, laptop);

            if ((Boolean) response.get("success")) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Помилка створення ноутбука: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/products/mobile-phone")
    public ResponseEntity<Map<String, Object>> createMobilePhone(
            @RequestParam Long adminId,
            @RequestBody Map<String, Object> phoneData) {

        try {
            MobilePhone phone = new MobilePhone(
                (String) phoneData.get("name"),
                Float.parseFloat(phoneData.get("price").toString()),
                (String) phoneData.get("firm"),
                Float.parseFloat(phoneData.get("maxDiscountPercentage").toString()),
                Boolean.parseBoolean(phoneData.get("isContract").toString()),
                Short.parseShort(phoneData.get("maxSimValue").toString()),
                (String) phoneData.get("imageURL")
            );
            
            if (phoneData.containsKey("quantity")) {
                phone.setQuantity(Integer.parseInt(phoneData.get("quantity").toString()));
            }

            Map<String, Object> response = adminService.createProduct(adminId, phone);

            if ((Boolean) response.get("success")) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Помилка створення мобільного телефону: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/products/smartphone")
    public ResponseEntity<Map<String, Object>> createSmartphone(
            @RequestParam Long adminId,
            @RequestBody Map<String, Object> smartphoneData) {

        try {
            @SuppressWarnings("unchecked")
            List<String> installedPrograms = (List<String>) smartphoneData.get("installedPrograms");
            
            Smartphone smartphone = new Smartphone(
                (String) smartphoneData.get("name"),
                Float.parseFloat(smartphoneData.get("price").toString()),
                (String) smartphoneData.get("firm"),
                Float.parseFloat(smartphoneData.get("maxDiscountPercentage").toString()),
                Boolean.parseBoolean(smartphoneData.get("isContract").toString()),
                Short.parseShort(smartphoneData.get("maxSimValue").toString()),
                (String) smartphoneData.get("OS"),
                installedPrograms,
                (String) smartphoneData.get("imageURL")
            );
            
            if (smartphoneData.containsKey("quantity")) {
                smartphone.setQuantity(Integer.parseInt(smartphoneData.get("quantity").toString()));
            }

            Map<String, Object> response = adminService.createProduct(adminId, smartphone);

            if ((Boolean) response.get("success")) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Помилка створення смартфону: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/products/stats")
    public ResponseEntity<Map<String, Object>> getProductStats() {
        Map<String, Object> stats = adminService.getProductStats();
        return ResponseEntity.ok(stats);
    }
} 