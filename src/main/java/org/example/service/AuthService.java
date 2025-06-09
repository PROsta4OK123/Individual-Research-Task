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
                // Витягуємо ID покупця з токену
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
        
        if (user instanceof Admin) {
            Admin admin = (Admin) user;
            userResponse.put("adminLevel", admin.getAdminLevel());
            userResponse.put("permissions", admin.getPermissions());
        }
        
        return userResponse;
    }
} 