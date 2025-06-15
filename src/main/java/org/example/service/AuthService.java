package org.example.service;

import org.example.entity.Admin;
import org.example.entity.User;
import org.example.entity.Customer;
import org.example.entity.RegularCustomer;
import org.example.repository.AdminRepository;
import org.example.repository.UserRepository;
import org.example.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    public Map<String, Object> registerUser(String username, String email, String password) {
        Map<String, Object> response = new HashMap<>();

        // Перевіряємо, чи існує користувач
        if (userRepository.existsByUsername(username)) {
            response.put("success", false);
            response.put("message", "Користувач з таким ім'ям уже існує");
            return response;
        }

        if (userRepository.existsByEmail(email)) {
            response.put("success", false);
            response.put("message", "Користувач з таким email уже існує");
            return response;
        }

        // Створюємо нового користувача
        User user = new User(username, email, passwordEncoder.encode(password), User.Role.USER);
        userRepository.save(user);

        // Створюємо відповідного постійного покупця з початковим балансом та ім'ям
        RegularCustomer regularCustomer = new RegularCustomer(5000.0, username + " (ID: " + user.getId() + ")", 0.0f);
        customerService.saveRegularCustomer(regularCustomer);

        response.put("success", true);
        response.put("message", "Постійний покупець успішно зареєстрований! Початковий баланс: 5000₴");
        response.put("userId", user.getId());
        response.put("customerId", regularCustomer.getId());

        return response;
    }

    public Map<String, Object> registerAdmin(String username, String email, String password, String adminKey) {
        Map<String, Object> response = new HashMap<>();

        // Перевіряємо адміністративний ключ
        if (!"ADMIN_SECRET_KEY_2024".equals(adminKey)) {
            response.put("success", false);
            response.put("message", "Неправильний адміністративний ключ");
            return response;
        }

        // Перевіряємо, чи існує користувач
        if (userRepository.existsByUsername(username)) {
            response.put("success", false);
            response.put("message", "Користувач з таким ім'ям уже існує");
            return response;
        }

        if (userRepository.existsByEmail(email)) {
            response.put("success", false);
            response.put("message", "Користувач з таким email уже існує");
            return response;
        }

        // Створюємо нового адміністратора
        Admin admin = new Admin(username, email, passwordEncoder.encode(password));
        adminRepository.save(admin);

        // Адміністратори також отримують статус постійного покупця, але з більшим балансом
        RegularCustomer adminCustomer = new RegularCustomer(20000.0, "👨‍💼 " + username + " (Адмін ID: " + admin.getId() + ")", 1000.0f);
        customerService.saveRegularCustomer(adminCustomer);

        response.put("success", true);
        response.put("message", "Адміністратор успішно зареєстрований! Баланс: 20000₴");
        response.put("adminId", admin.getId());
        response.put("customerId", adminCustomer.getId());

        return response;
    }

    public Map<String, Object> authenticate(String username, String password) {
        Map<String, Object> response = new HashMap<>();

        Optional<User> userOpt = userRepository.findByUsernameOrEmail(username, username);
        
        if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPassword())) {
            response.put("success", false);
            response.put("message", "Неправильне ім'я користувача або пароль");
            return response;
        }

        User user = userOpt.get();
        
        if (!user.isActive()) {
            response.put("success", false);
            response.put("message", "Аккаунт деактивовано");
            return response;
        }

        // Оновлюємо час останнього входу для адміна
        if (user instanceof Admin) {
            Admin admin = (Admin) user;
            admin.setLastLogin(LocalDateTime.now());
            adminRepository.save(admin);
        }

        // Шукаємо відповідного покупця
        RegularCustomer customer = findCustomerByUserId(user.getId());
        if (customer == null) {
            // Якщо покупець не знайдений, створюємо його
            customer = new RegularCustomer(5000.0, user.getUsername() + " (ID: " + user.getId() + ")", 0.0f);
            customerService.saveRegularCustomer(customer);
        }

        // Генеруємо JWT токен
        String token = jwtUtils.generateJwtToken(user.getUsername(), user.getRole().toString(), user.getId());

        response.put("success", true);
        response.put("message", "Аутентифікація успішна");
        response.put("token", token);
        response.put("user", createUserResponse(user, customer));

        return response;
    }

    public Map<String, Object> loginAsGuest() {
        Map<String, Object> response = new HashMap<>();

        // Створюємо тимчасового гостьового покупця
        Customer guestCustomer = new Customer(2000.0); // Гість отримує менший баланс
        customerService.saveCustomer(guestCustomer);

        // Створюємо фіктивного користувача для гостя
        Map<String, Object> guestUser = new HashMap<>();
        guestUser.put("id", guestCustomer.getId());
        guestUser.put("username", "Гість #" + guestCustomer.getId());
        guestUser.put("email", "guest@temp.com");
        guestUser.put("role", "GUEST");
        guestUser.put("customerId", guestCustomer.getId());
        guestUser.put("customerType", "Customer");
        guestUser.put("balance", guestCustomer.getMoney());
        guestUser.put("discount", 0.0);

        response.put("success", true);
        response.put("message", "Вхід як гість виконано! Баланс: 2000₴, знижки відсутні");
        response.put("token", "guest_" + guestCustomer.getId()); // Простий токен для гостя
        response.put("user", guestUser);

        return response;
    }

    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public Map<String, Object> deleteGuestAccount(String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Перевіряємо, що це гостьовий токен
            if (token != null && token.startsWith("guest_")) {
                // Витягуємо ID покупця з токена
                String customerIdStr = token.replace("guest_", "");
                Long customerId = Long.parseLong(customerIdStr);
                
                // Видаляємо гостьового покупця
                Optional<Customer> guestCustomer = customerService.getCustomerById(customerId);
                if (guestCustomer.isPresent()) {
                    customerService.deleteCustomer(customerId);
                    response.put("success", true);
                    response.put("message", "Гостьовий аккаунт успішно видалено");
                } else {
                    response.put("success", false);
                    response.put("message", "Гостьовий аккаунт не знайдено");
                }
            } else {
                response.put("success", false);
                response.put("message", "Недопустимий гостьовий токен");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Помилка при видаленні гостьового аккаунту: " + e.getMessage());
        }
        
        return response;
    }

    private RegularCustomer findCustomerByUserId(Long userId) {
        // Шукаємо покупця за ім'ям (містить ID користувача)
        String searchPattern = "(ID: " + userId + ")";
        return customerService.getAllRegularCustomers().stream()
                .filter(customer -> customer.getFullName().contains(searchPattern))
                .findFirst()
                .orElse(null);
    }

    public Map<String, Object> getCurrentUserInfo(Long customerId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Customer> customerOpt = customerService.getCustomerById(customerId);
            if (customerOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Користувач не знайдений");
                return response;
            }
            
            Customer customer = customerOpt.get();
            
            response.put("success", true);
            response.put("balance", customer.getMoney());
            response.put("discount", customer.getIndividualDiscount());
            
            if (customer instanceof RegularCustomer regularCustomer) {
                response.put("totalPurchases", regularCustomer.getTotalPurchasesAmount());
                response.put("customerType", "RegularCustomer");
            } else {
                response.put("totalPurchases", 0.0);
                response.put("customerType", "Customer");
            }
            
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Помилка отримання інформації: " + e.getMessage());
            return response;
        }
    }

    private Map<String, Object> createUserResponse(User user, RegularCustomer customer) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("username", user.getUsername());
        userResponse.put("email", user.getEmail());
        userResponse.put("role", user.getRole().toString());
        userResponse.put("customerId", customer.getId());
        userResponse.put("customerType", "RegularCustomer");
        userResponse.put("balance", customer.getMoney());
        userResponse.put("discount", customer.getIndividualDiscount());
        userResponse.put("totalPurchases", customer.getTotalPurchasesAmount());
        
        if (user instanceof Admin admin) {
            userResponse.put("adminLevel", admin.getAdminLevel());
            userResponse.put("permissions", admin.getPermissions());
        }
        
        return userResponse;
    }

    public Map<String, Object> validateToken(String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Перевіряємо гостьовий токен
            if (token != null && token.startsWith("guest_")) {
                String customerIdStr = token.replace("guest_", "");
                Long customerId = Long.parseLong(customerIdStr);
                
                // Перевіряємо, чи існує гостьовий покупець
                Optional<Customer> guestCustomer = customerService.getCustomerById(customerId);
                if (guestCustomer.isPresent()) {
                    response.put("success", true);
                    response.put("message", "Гостьовий токен дійсний");
                    response.put("tokenType", "GUEST");
                } else {
                    response.put("success", false);
                    response.put("message", "Гостьовий акаунт не існує");
                }
                return response;
            }
            
            // Перевіряємо JWT токен
            if (!jwtUtils.validateJwtToken(token)) {
                response.put("success", false);
                response.put("message", "Токен недійсний або прострочений");
                return response;
            }
            
            // Якщо токен дійсний, отримуємо інформацію з нього
            String username = jwtUtils.getUserNameFromJwtToken(token);
            Long userId = jwtUtils.getUserIdFromJwtToken(token);
            String role = jwtUtils.getRoleFromJwtToken(token);
            
            // Перевіряємо, чи існує користувач
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Користувач не знайдений");
                return response;
            }
            
            User user = userOpt.get();
            if (!user.isActive()) {
                response.put("success", false);
                response.put("message", "Аккаунт деактивовано");
                return response;
            }
            
            response.put("success", true);
            response.put("message", "Токен дійсний");
            response.put("tokenType", "JWT");
            response.put("username", username);
            response.put("userId", userId);
            response.put("role", role);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Помилка валідації токена: " + e.getMessage());
        }
        
        return response;
    }

    public Map<String, Object> refreshToken(String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Перевіряємо гостьовий токен - для гостей оновлення не потрібне
            if (token != null && token.startsWith("guest_")) {
                response.put("success", false);
                response.put("message", "Гостьові токени не потребують оновлення");
                return response;
            }
            
            // Перевіряємо JWT токен (навіть якщо він трохи прострочений)
            String username = null;
            Long userId = null;
            String role = null;
            
            try {
                username = jwtUtils.getUserNameFromJwtToken(token);
                userId = jwtUtils.getUserIdFromJwtToken(token);
                role = jwtUtils.getRoleFromJwtToken(token);
            } catch (Exception e) {
                // Якщо токен прострочений, але ще можна витягти дані
                response.put("success", false);
                response.put("message", "Токен занадто старий для оновлення");
                return response;
            }
            
            if (username == null || userId == null) {
                response.put("success", false);
                response.put("message", "Неможливо витягти дані з токена");
                return response;
            }
            
            // Перевіряємо, чи існує користувач
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Користувач не знайдений");
                return response;
            }
            
            User user = userOpt.get();
            if (!user.isActive()) {
                response.put("success", false);
                response.put("message", "Аккаунт деактивовано");
                return response;
            }
            
            // Генеруємо новий токен
            String newToken = jwtUtils.generateJwtToken(username, role, userId);
            
            // Отримуємо оновлену інформацію про користувача
            RegularCustomer customer = findCustomerByUserId(userId);
            if (customer == null) {
                response.put("success", false);
                response.put("message", "Дані покупця не знайдено");
                return response;
            }
            
            response.put("success", true);
            response.put("message", "Токен успішно оновлено");
            response.put("token", newToken);
            response.put("user", createUserResponse(user, customer));
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Помилка оновлення токена: " + e.getMessage());
        }
        
        return response;
    }
} 