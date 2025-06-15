# 🔧 Технічний посібник VoiceMood

## 📐 Архітектурні рішення

### Патерни проектування
- **MVC (Model-View-Controller)** - основна архітектура
- **Repository Pattern** - доступ до даних через Spring Data JPA
- **Service Layer Pattern** - бізнес-логіка винесена в сервіси
- **DTO Pattern** - передача даних між шарами
- **Strategy Pattern** - обробка різних типів продуктів

### Структура шарів
```
┌─────────────────┐
│   Presentation  │ ← Controllers, REST API
├─────────────────┤
│    Service      │ ← Business Logic
├─────────────────┤
│   Repository    │ ← Data Access Layer
├─────────────────┤
│     Entity      │ ← Domain Models
└─────────────────┘
```

---

## 📦 Структура entities

### Базова ієрархія продуктів
```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "products")
public abstract class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private Double price;
    
    // Спільні поля для всіх продуктів
}

@Entity
@Table(name = "laptops")
public class Laptop extends Product {
    private Double diagonal;
    private Double weight;
    private Integer coreNumber;
    private Integer memory;
}
```

### Система користувачів
```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "users")
public class User {
    @Enumerated(EnumType.STRING)
    private Role role; // USER, ADMIN
    
    private boolean active = true;
}

@Entity
@Table(name = "admins")
public class Admin extends User {
    private Integer adminLevel;
    private String permissions;
    private LocalDateTime lastLogin;
}
```

---

## 🔒 Система безпеки

### JWT Implementation
```java
@Component
public class JwtUtils {
    private SecretKey getSigningKey() {
        if (jwtSecret.length() < 64) {
            throw new IllegalArgumentException(
                "JWT secret must be at least 64 characters for HS512"
            );
        }
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    public String generateJwtToken(String username, String role, Long userId) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
}
```

---

## 📊 База даних

### Схема таблиць
```sql
-- Основні таблиці
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('USER', 'ADMIN') NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    firm VARCHAR(255),
    image_url TEXT,
    quantity INTEGER DEFAULT 0,
    discount_percent DECIMAL(5,2) DEFAULT 0,
    product_type VARCHAR(50) NOT NULL
);
```

---

## 🚀 API Design

### RESTful Convention
```
GET    /api/products       - Отримати всі продукти
GET    /api/products/{id}  - Отримати продукт за ID
POST   /api/products       - Створити новий продукт (admin)
PUT    /api/products/{id}  - Оновити продукт (admin)
DELETE /api/products/{id}  - Видалити продукт (admin)

POST   /api/purchases/buy  - Здійснити покупку
GET    /api/purchases/history/{customerId} - Історія покупок
```

### Response Format
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    // Response data
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

## 🧪 Тестування

### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @InjectMocks
    private ProductService productService;
    
    @Test
    void shouldReturnAllProducts() {
        // Given
        List<Product> mockProducts = Arrays.asList(
            new Laptop("MacBook", 25000.0, "Apple")
        );
        when(productRepository.findAll()).thenReturn(mockProducts);
        
        // When
        List<Product> result = productService.getAllProducts();
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("MacBook");
    }
}
```

---

## 📱 Frontend Architecture

### JavaScript Modules
```javascript
// app.js - Головний файл
const API_BASE = '/api';
let currentUser = null;

// auth.js - Автентифікація
function login() { /* ... */ }
function logout() { /* ... */ }

// products.js - Управління продуктами
function loadProducts() { /* ... */ }
function showProductDetails() { /* ... */ }

// admin.js - Адміністративні функції
function createProduct() { /* ... */ }
function updateQuantity() { /* ... */ }
```

---

## 🔧 Налаштування розробки

### Environment Profiles
```properties
# application-dev.properties
spring.jpa.show-sql=true
logging.level.org.springframework.web=DEBUG
app.jwt.expiration=7200000

# application-prod.properties
spring.jpa.show-sql=false
logging.level.org.springframework.web=WARN
app.jwt.expiration=1200000

# application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
```

---

## 🚀 Deployment Strategies

### Docker Configuration
```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim

# Create app directory
WORKDIR /app

# Copy jar file
COPY target/voicemood-1.0.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 🔮 Розширення системи

### Додавання нових типів продуктів
```java
// 1. Створити новий entity
@Entity
@Table(name = "tablets")
public class Tablet extends Product {
    private Double screenSize;
    private String operatingSystem;
    private Boolean hasKeyboard;
    
    // constructors, getters, setters
}

// 2. Додати в ProductService
public Map<String, Object> createTablet(TabletRequest request) {
    Tablet tablet = new Tablet(
        request.getName(),
        request.getPrice(),
        request.getFirm(),
        request.getScreenSize(),
        request.getOperatingSystem(),
        request.getHasKeyboard()
    );
    
    tablet.setQuantity(request.getQuantity());
    tablet.setImageURL(request.getImageURL());
    
    productRepository.save(tablet);
    return Map.of("success", true, "message", "Tablet created successfully");
}

// 3. Додати endpoint в AdminController
@PostMapping("/products/tablet")
public ResponseEntity<Map<String, Object>> createTablet(@RequestBody TabletRequest request) {
    return ResponseEntity.ok(adminService.createTablet(request));
}
```

---

---

## 🔄 Останні технічні зміни (2025-06-10)

### ✅ Виправлений PurchaseController
**Проблема:** Контролер повертав мінімальні дані, що призводило до JavaScript помилок.

**Рішення:** Оновлений response format:
```java
@PostMapping
public ResponseEntity<Map<String, Object>> makePurchase(
        @RequestParam Long customerId, 
        @RequestParam Long productId) {
    
    // Отримуємо дані ПЕРЕД покупкою для розрахунків
    Customer customer = customerService.getCustomerById(customerId).get();
    Product product = productService.getProductById(productId).get();
    
    // Розрахунки знижок
    double originalPrice = product.getPrice();
    float appliedDiscount = Math.min(
        customer.getIndividualDiscount(), 
        product.getMaxDiscountPercentage()
    );
    double finalPrice = originalPrice * (1 - appliedDiscount / 100.0);
    
    // Виконуємо покупку
    boolean success = purchaseService.makePurchase(customerId, productId);
    
    if (success) {
        Customer updatedCustomer = customerService.getCustomerById(customerId).get();
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Покупка успішно завершена!",
            "productName", product.getName(),
            "productFirm", product.getFirm(),
            "originalPrice", originalPrice,
            "finalPrice", finalPrice,
            "appliedDiscount", appliedDiscount,
            "remainingMoney", updatedCustomer.getMoney()
        ));
    }
}
```

### ✅ Виправлені URL'и для історії покупок
**Проблема:** JavaScript використовував query параметри замість path параметрів.

**Рішення:** Оновлені endpoint'и в history.js:
```javascript
// Було (неправильно):
fetch(`/api/purchases/history?customerId=${customerId}`)

// Стало (правильно):
fetch(`/api/purchases/history/${customerId}`)
```

### ✅ Додано версіонування статичних файлів
**Проблема:** Кеш браузера не оновлювався після змін JavaScript.

**Рішення:** Додано версії до всіх JS файлів:
```html
<script src="js/app.js?v=1.1"></script>
<script src="js/history.js?v=1.1"></script>
<script src="js/purchase.js?v=1.1"></script>
```

### 🔧 Додані безпечні методи
Додано перевірки в JavaScript для уникнення undefined помилок:
```javascript
// Безпечне використання toFixed()
if (data.finalPrice !== undefined && data.finalPrice !== null) {
    const priceText = data.finalPrice.toFixed(2);
}

// Перевірка success статусу
if (data.success) {
    displayPurchaseHistory(data.purchases);
} else {
    throw new Error(data.message || 'Невідома помилка');
}
```

---

## 📚 Рекомендації для розвитку

### 🎯 Можливі покращення
1. **Додати валідацію на рівні entity:**
   ```java
   @Entity
   public class Product {
       @NotBlank(message = "Назва товару обов'язкова")
       private String name;
       
       @Min(value = 0, message = "Ціна не може бути від'ємною")
       private Double price;
   }
   ```

2. **Реалізувати кешування:**
   ```java
   @Service
   public class ProductService {
       @Cacheable("products")
       public List<Product> getAllProducts() {
           return productRepository.findAll();
       }
   }
   ```

3. **Додати логування:**
   ```java
   @Service
   @Slf4j
   public class PurchaseService {
       public boolean makePurchase(Long customerId, Long productId) {
           log.info("Processing purchase: customerId={}, productId={}", 
               customerId, productId);
           // ...
       }
   }
   ```

**🔧 Цей технічний посібник допоможе розробникам швидко зрозуміти архітектуру та розширити функціональність VoiceMood! 🔧** 