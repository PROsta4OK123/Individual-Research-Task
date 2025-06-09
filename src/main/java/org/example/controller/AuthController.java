package org.example.controller;

import org.example.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");

        if (username == null || email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Всі поля обов'язкові для заповнення"
            ));
        }

        Map<String, Object> response = authService.registerUser(username, email, password);
        
        if ((Boolean) response.get("success")) {
            log.info("Successful registration of user " + username + " with email " + email);
            return ResponseEntity.ok(response);
        } else {
            log.error("Failed registration of user " + username + " with email " + email);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/register-admin")
    public ResponseEntity<Map<String, Object>> registerAdmin(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");
        String adminKey = request.get("adminKey");

        if (username == null || email == null || password == null || adminKey == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Всі поля обов'язкові для заповнення"
            ));
        }

        Map<String, Object> response = authService.registerAdmin(username, email, password, adminKey);
        
        if ((Boolean) response.get("success")) {
            log.info("Successful registration of admin " + username + " with email " + email);
            return ResponseEntity.ok(response);
        } else {
            log.error("Failed registration of admin " + username + " with email " + email);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> authenticateUser(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Ім'я користувача та пароль обов'язкові"
            ));
        }

        Map<String, Object> response = authService.authenticate(username, password);
        
        if ((Boolean) response.get("success")) {
            log.info("Successful authentication of user " + username);
            return ResponseEntity.ok(response);
        } else {
            log.error("Failed authentication of user " + username);
            return ResponseEntity.status(401).body(response);
        }
    }

    @PostMapping("/guest-login")
    public ResponseEntity<Map<String, Object>> loginAsGuest() {
        Map<String, Object> response = authService.loginAsGuest();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/guest-logout")
    public ResponseEntity<Map<String, Object>> logoutGuest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, String> requestBody) {
        
        String token = null;
        
        // Намагаємося отримати токен з заголовка
        if (authHeader != null) {
            if (authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            } else {
                token = authHeader;
            }
        }
        
        // Якщо токена немає в заголовку, намагаємося отримати з тіла запиту
        if (token == null && requestBody != null) {
            token = requestBody.get("token");
        }

        if (token == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Токен не надано"
            ));
        }

        Map<String, Object> response = authService.deleteGuestAccount(token);
        
        if ((Boolean) response.get("success")) {
            log.info("Successful logout of guest account");
            return ResponseEntity.ok(response);
        } else {
            log.error("Failed logout of guest account");
            return ResponseEntity.badRequest().body(response);
        }
    }
} 