# 🛍️ VoiceMood - Система електронної комерції
## 📚 Документація для початківців

> **💡 Примітка:** Цей проект розроблений з урахуванням потреб тих, хто тільки починає вивчати Java та веб-розробку. Усі концепції пояснені простою мовою з прикладами.

---

## 🎯 Що таке VoiceMood?

VoiceMood - це **повнофункціональний інтернет-магазин**, написаний на Java з використанням фреймворку Spring Boot. Уявіть собі сайт на кшталт Rozetka або Amazon, але в спрощеному варіанті для навчання.

### 🔍 Що можна робити в системі?
- 🛒 **Переглядати товари** (ноутбуки, телефони, смартфони)
- 💳 **Купувати товари** зі своєго віртуального балансу
- 👤 **Реєструватися та входити** в систему
- 📊 **Переглядати історію покупок** і статистику
- 🎁 **Отримувати знижки** за активні покупки
- 👨‍💼 **Керувати магазином** (для адміністраторів)

---

## 🧩 Як працює система? (Архітектура для новачків)

### 🏗️ Що таке Spring Boot?
**Spring Boot** - це спеціальний набір інструментів для Java, який допомагає швидко створювати веб-додатки. Думайте про це як про "конструктор" для програм.

### 📦 Структура проекту (що де знаходиться)
```
VoiceMood/
├── 📁 src/main/java/org/example/     ← Тут весь Java код
│   ├── 📄 entity/                    ← "Моделі" (що зберігаємо в БД)
│   ├── 📄 repository/                ← "Сховище" (як працюємо з БД)
│   ├── 📄 service/                   ← "Бізнес-логіка" (основні дії)
│   ├── 📄 controller/                ← "API" (що відповідає на запити)
│   ├── 📄 security/                  ← "Безпека" (хто може що робити)
│   └── 📄 Application.java           ← Головний файл (запуск програми)
├── 📁 src/main/resources/            ← Налаштування та веб-файли
│   ├── 📁 static/                    ← Веб-сторінки (HTML, CSS, JS)
│   └── 📄 application.properties     ← Налаштування системи
└── 📄 pom.xml                        ← Список бібліотек (як package.json)
```

### 🔄 Як компоненти взаємодіють між собою?
```
Користувач → Веб-сторінка → Controller → Service → Repository → База даних
    ↑                                                              ↓
    ←――――――――――――― Відповідь ←――――――――――――――――――――――――――――――――――――――
```

1. **Користувач** натискає кнопку на веб-сторінці
2. **JavaScript** відправляє запит на сервер
3. **Controller** приймає запит і розуміє, що треба зробити
4. **Service** виконує бізнес-логіку (перевірки, розрахунки)
5. **Repository** звертається до бази даних
6. Результат повертається назад до користувача

---

## 🎓 Детальний розбір коду (для початківців)

### 📋 Entity (Сутності) - Що зберігаємо?

Entity - це класи, які описують, **що саме ми зберігаємо в базі даних**. Кожен клас = одна таблиця в БД.

#### 👤 Клас Customer (Покупець)
```java
@Entity  // ← Каже Spring: "Це таблиця в БД"
public class Customer {
    @Id  // ← Це головний ключ (унікальний номер)
    @GeneratedValue  // ← Автоматично генерувати номер
    private Long id;
    
    private String username;     // Логін користувача
    private double money;        // Скільки грошей у користувача
    private float discount;      // Знижка у відсотках
    
    // Конструктори, геттери та сеттери...
}
```

**🤔 Чому це потрібно?** 
- Кожен покупець має свій унікальний номер (id)
- Система запам'ятовує баланс і знижки кожного користувача
- Java автоматично створює таблицю в БД на основі цього класу

#### 🛒 Клас Product (Товар) - Наслідування
```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)  // ← Як зберігати наслідування в БД
public abstract class Product {
    @Id
    @GeneratedValue
    private Long id;
    
    private String name;         // Назва товару
    private double price;        // Ціна
    private String firm;         // Виробник
    private int quantity;        // Кількість на складі
    
    // getters/setters...
}
```

**🎯 Наслідування простими словами:**
Product - це "загальне поняття товару". Але конкретно ми продаємо ноутбуки, телефони тощо.

```java
@Entity
public class Laptop extends Product {  // ← Laptop "наслідує" від Product
    private double diagonal;    // Діагональ екрану
    private double weight;      // Вага
    private int coreNumber;     // Кількість ядер процесора
    private int memory;         // Обсяг пам'яті
}
```

**✅ Переваги наслідування:**
- Не треба повторювати код (name, price вже є в Product)
- Можна працювати з усіма товарами однаково
- Легко додавати нові типи товарів

### 🗄️ Repository (Репозиторій) - Як працювати з БД?

Repository - це "посередник" між вашим кодом і базою даних. Spring автоматично створює методи для роботи з БД.

```java
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // JpaRepository вже має готові методи:
    // save(), findById(), findAll(), delete() тощо
    
    // Можна додавати свої методи:
    Optional<Customer> findByUsername(String username);
    // ↑ Spring автоматично розуміє: "знайди покупця за логіном"
    
    @Query("SELECT c FROM Customer c WHERE c.money > :amount")
    List<Customer> findRichCustomers(@Param("amount") double amount);
    // ↑ Власний SQL-запит: знайти покупців з балансом більше певної суми
}
```

**🔍 Що тут відбувається?**
- `JpaRepository<Customer, Long>` - працюємо з таблицею Customer, ключ типу Long
- Spring **автоматично** створює реалізацію всіх методів
- Назва методу `findByUsername` автоматично перетворюється в SQL-запит

### 🎯 Service (Сервіс) - Бізнес-логіка

Service містить **основну логіку програми** - що і як робити.

```java
@Service  // ← Каже Spring: "Це сервіс з бізнес-логікою"
@Transactional  // ← Якщо щось пішло не так - відкатити зміни в БД
public class PurchaseService {
    
    @Autowired  // ← Spring автоматично підключає залежності
    private CustomerService customerService;
    
    @Autowired
    private ProductService productService;
    
    public boolean makePurchase(Long customerId, Long productId) {
        // 1. Знаходимо покупця
        Customer customer = customerService.getCustomerById(customerId)
            .orElseThrow(() -> new RuntimeException("Покупець не знайдений"));
        
        // 2. Знаходимо товар
        Product product = productService.getProductById(productId)
            .orElseThrow(() -> new RuntimeException("Товар не знайдений"));
        
        // 3. Перевіряємо, чи є товар на складі
        if (product.getQuantity() <= 0) {
            throw new RuntimeException("Товару немає на складі");
        }
        
        // 4. Розраховуємо знижку
        float customerDiscount = customer.getIndividualDiscount();
        float productMaxDiscount = product.getMaxDiscountPercentage();
        float appliedDiscount = Math.min(customerDiscount, productMaxDiscount);
        
        // 5. Розраховуємо фінальну ціну
        double originalPrice = product.getPrice();
        double finalPrice = originalPrice * (1 - appliedDiscount / 100.0);
        
        // 6. Перевіряємо, чи вистачає грошей
        if (customer.getMoney() >= finalPrice) {
            // 7. Списуємо гроші
            customer.setMoney(customer.getMoney() - finalPrice);
            
            // 8. Зменшуємо кількість товару
            product.setQuantity(product.getQuantity() - 1);
            
            // 9. Зберігаємо зміни
            customerService.saveCustomer(customer);
            productService.saveProduct(product);
            
            return true;  // Покупка успішна
        } else {
            throw new RuntimeException("Недостатньо коштів");
        }
    }
}
```

**🧠 Чому саме так?**
- Вся логіка покупки зібрана в одному місці
- `@Transactional` гарантує: або всі зміни зберігаються, або жодна
- Кожен крок логічний і зрозумілий

### 🌐 Controller (Контролер) - API для фронтенду

Controller обробляє **HTTP-запити** з веб-сторінки і повертає відповіді.

```java
@RestController  // ← Це REST API контролер
@RequestMapping("/api/purchases")  // ← Всі методи будуть починатися з /api/purchases
@CrossOrigin(origins = "*")  // ← Дозволити запити з будь-яких доменів
public class PurchaseController {
    
    @Autowired
    private PurchaseService purchaseService;
    
    @PostMapping  // ← Обробляє POST запити на /api/purchases
    public ResponseEntity<Map<String, Object>> makePurchase(
            @RequestParam Long customerId,   // ← Параметр з URL: ?customerId=123
            @RequestParam Long productId) {  // ← Параметр з URL: ?productId=456
        
        try {
            // Отримуємо дані ПЕРЕД покупкою (для відповіді)
            Customer customer = customerService.getCustomerById(customerId).get();
            Product product = productService.getProductById(productId).get();
            
            // Розрахунки для відповіді
            double originalPrice = product.getPrice();
            float appliedDiscount = Math.min(
                customer.getIndividualDiscount(), 
                product.getMaxDiscountPercentage()
            );
            double finalPrice = originalPrice * (1 - appliedDiscount / 100.0);
            
            // Виконуємо покупку
            boolean success = purchaseService.makePurchase(customerId, productId);
            
            if (success) {
                // Отримуємо оновлені дані покупця
                Customer updatedCustomer = customerService.getCustomerById(customerId).get();
                
                // Повертаємо детальну відповідь
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Покупка успішно завершена!",
                    "productName", product.getName(),
                    "originalPrice", originalPrice,
                    "finalPrice", finalPrice,
                    "appliedDiscount", appliedDiscount,
                    "remainingMoney", updatedCustomer.getMoney()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Покупка не вдалася"
                ));
            }
        } catch (Exception e) {
            // Якщо сталася помилка - повертаємо детальну інформацію
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}
```

**🔗 Як це працює з фронтендом?**
JavaScript робить запит:
```javascript
fetch('/api/purchases?customerId=123&productId=456', { method: 'POST' })
```

Сервер повертає JSON:
```json
{
  "success": true,
  "message": "Покупка успішно завершена!",
  "productName": "iPhone 14",
  "originalPrice": 25000.0,
  "finalPrice": 22500.0,
  "appliedDiscount": 10.0,
  "remainingMoney": 7500.0
}
```

---

## 🔐 JWT Автентифікація (Безпека для початківців)

### 🤔 Що таке JWT?
**JWT (JSON Web Token)** - це "цифровий пропуск", який довіряє, що ви - це ви.

**Простими словами:**
1. Ви вводите логін/пароль
2. Сервер перевіряє і дає вам "токен" (зашифрований текст)
3. При кожному запиті ви показуєте цей токен
4. Сервер розшифровує і розуміє, хто ви і що можете робити

### 🔧 Налаштування JWT
```properties
# application.properties
jwt.secret=MyVeryLongAndSecureSecretKeyForJWTTokenGenerationThatShouldBeAtLeast64Characters!!
jwt.expiration=1200000  # 20 хвилин у мілісекундах
```

### 🛡️ JWT Утиліти
```java
@Component
public class JwtUtils {
    
    @Value("${jwt.secret}")  // ← Читаємо секретний ключ з application.properties
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private int jwtExpirationMs;
    
    // Створити токен для користувача
    public String generateJwtToken(String username, String role) {
        return Jwts.builder()
            .setSubject(username)                    // Логін користувача
            .claim("role", role)                     // Роль (USER/ADMIN)
            .setIssuedAt(new Date())                 // Коли створено
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))  // Коли закінчується
            .signWith(SignatureAlgorithm.HS512, jwtSecret)  // Підписати секретним ключем
            .compact();
    }
    
    // Отримати логін з токена
    public String getUsernameFromJwtToken(String token) {
        return Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }
    
    // Перевірити, чи токен ще дійсний
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;  // Токен недійсний
        }
    }
}
```

### 🔄 Автоматичне оновлення токенів
```javascript
// JavaScript код на фронтенді
async function refreshTokenIfNeeded() {
    const token = localStorage.getItem('authToken');
    const expiryTime = getTokenExpiryTime(token);  // Отримуємо час закінчення
    const currentTime = new Date();
    const timeLeft = expiryTime - currentTime;
    const fifteenMinutes = 15 * 60 * 1000;  // 15 хвилин
    
    // Якщо залишилось менше 15 хвилин - оновлюємо токен
    if (timeLeft < fifteenMinutes && timeLeft > 0) {
        const response = await fetch('/api/auth/refresh-token', {
            method: 'POST',
            headers: { 'Authorization': token }
        });
        
        const data = await response.json();
        if (data.success) {
            localStorage.setItem('authToken', data.token);  // Зберігаємо новий токен
        }
    }
}

// Перевіряємо кожні 2 хвилини
setInterval(refreshTokenIfNeeded, 2 * 60 * 1000);
```

---

## 🎨 Фронтенд (Веб-інтерфейс для початківців)

### 📁 Структура фронтенду
```
static/
├── 📄 index.html          ← Головна сторінка
├── 📁 css/
│   └── 📄 style.css       ← Стилі оформлення
└── 📁 js/
    ├── 📄 app.js          ← Основна логіка
    ├── 📄 auth.js         ← Автентифікація
    ├── 📄 products.js     ← Робота з товарами
    ├── 📄 purchase.js     ← Покупки
    ├── 📄 history.js      ← Історія покупок
    └── 📄 admin.js        ← Адмін-панель
```

### 🔗 Як фронтенд спілкується з бекендом?
```javascript
// Приклад покупки товару
async function buyProduct(customerId, productId) {
    try {
        // Відправляємо POST запит на сервер
        const response = await fetch(`/api/purchases?customerId=${customerId}&productId=${productId}`, {
            method: 'POST',
            headers: {
                'Authorization': localStorage.getItem('authToken'),  // JWT токен
                'Content-Type': 'application/json'
            }
        });
        
        // Отримуємо відповідь у форматі JSON
        const data = await response.json();
        
        if (data.success) {
            // Показуємо успішне повідомлення
            showMessage(`✅ Покупка успішна! Залишок: ${data.remainingMoney}₴`);
            updateUserBalance(data.remainingMoney);  // Оновлюємо баланс на сторінці
        } else {
            // Показуємо помилку
            showMessage(`❌ Помилка: ${data.message}`);
        }
    } catch (error) {
        console.error('Помилка покупки:', error);
        showMessage('❌ Помилка з\'єднання з сервером');
    }
}
```

### 🗂️ Управління станом додатку
```javascript
// Глобальні змінні для збереження стану
let currentUser = null;        // Поточний користувач
let selectedProductId = null;  // Обраний товар
let products = [];             // Список всіх товарів

// Функція для оновлення інформації про користувача
function updateUserInfo() {
    if (!currentUser) return;
    
    let userInfo = '';
    if (currentUser.role === 'ADMIN') {
        userInfo = `👨‍💼 ${currentUser.username} | Баланс: ${currentUser.balance.toFixed(2)}₴`;
    } else {
        userInfo = `👤 ${currentUser.username} | Баланс: ${currentUser.balance.toFixed(2)}₴ | Знижка: ${currentUser.discount.toFixed(1)}%`;
    }
    
    document.getElementById('userInfo').innerHTML = userInfo;
}
```

---

## 🚀 Запуск проекту (Покрокова інструкція)

### 📋 Передумови
1. **Java 17+** - мова програмування
2. **MySQL 8.0+** - база даних
3. **Maven 3.6+** - система збірки (зазвичай вбудована в IDE)
4. **IDE** - IntelliJ IDEA або Eclipse

### 🗄️ Налаштування бази даних
1. **Встановіть MySQL** і створіть базу даних:
```sql
CREATE DATABASE voicemood_db;
CREATE USER 'voicemood_user'@'localhost' IDENTIFIED BY 'password123';
GRANT ALL PRIVILEGES ON voicemood_db.* TO 'voicemood_user'@'localhost';
FLUSH PRIVILEGES;
```

2. **Налаштуйте application.properties:**
```properties
# Підключення до бази даних
spring.datasource.url=jdbc:mysql://localhost:3306/voicemood_db
spring.datasource.username=voicemood_user
spring.datasource.password=password123

# Автоматичне створення таблиць
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT налаштування
jwt.secret=MyVeryLongAndSecureSecretKeyForJWTTokenGenerationThatShouldBeAtLeast64Characters!!
jwt.expiration=1200000
```

### ▶️ Запуск програми
1. **З командного рядка:**
```bash
mvn spring-boot:run
```

2. **З IDE:** Запустіть клас `Application.java`

3. **Відкрийте браузер:** http://localhost:8080

### 👥 Тестові користувачі
```
Звичайний користувач:
├── Логін: testuser
└── Пароль: password123

Адміністратор:
├── Логін: admin
└── Пароль: admin123

Гість:
└── Кнопка "Увійти як гість" (без пароля)
```

---

## 🔧 Виправлення частих проблем

### ❌ Проблема: "Помилка завантаження історії покупок"
**Причина:** Неправильні URL'и в JavaScript або кеш браузера

**Рішення:**
1. Перевірте файл `history.js` - URL повинні бути:
   ```javascript
   fetch(`/api/purchases/history/${customerId}`)      // ✅ Правильно
   fetch(`/api/purchases/statistics/${customerId}`)   // ✅ Правильно
   ```

2. Очистіть кеш браузера: `Ctrl+Shift+R`

### ❌ Проблема: "Cannot read properties of undefined (reading 'toFixed')"
**Причина:** Сервер повертає дані не в тому форматі, який очікує JavaScript

**Рішення:**
1. Перевірте, що Controller повертає всі необхідні поля:
   ```java
   return ResponseEntity.ok(Map.of(
       "success", true,
       "finalPrice", finalPrice,        // ← Обов'язково число
       "remainingMoney", customerAfter.getMoney()  // ← Обов'язково число
   ));
   ```

2. У JavaScript додайте перевірки:
   ```javascript
   if (data.finalPrice !== undefined) {
       const priceText = data.finalPrice.toFixed(2);
   }
   ```

### ❌ Проблема: 404 NOT FOUND для API
**Причина:** Endpoint не існує або неправильний URL

**Рішення:**
1. Перевірте анотації в Controller:
   ```java
   @RestController
   @RequestMapping("/api/purchases")  // ← Базовий шлях
   public class PurchaseController {
       
       @PostMapping  // ← POST /api/purchases
       public ResponseEntity<Map<String, Object>> makePurchase(...) {
   ```

2. Перевірте JavaScript запит:
   ```javascript
   fetch('/api/purchases?customerId=123&productId=456', { method: 'POST' })
   ```

### ❌ Проблема: Кеш браузера не оновлюється
**Рішення:** Додайте версії до JavaScript файлів:
```html
<script src="js/app.js?v=1.1"></script>
<script src="js/history.js?v=1.1"></script>
```

### ⚡ Останні виправлення (2025-06-10)

#### 🔧 Виправлено PurchaseController
- **Проблема:** Контролер повертав тільки `success` і `message`
- **Рішення:** Додано детальну інформацію про покупку:
  ```java
  return ResponseEntity.ok(Map.of(
      "success", true,
      "productName", product.getName(),
      "finalPrice", finalPrice,
      "remainingMoney", customerAfter.getMoney()
  ));
  ```

#### 🔗 Виправлено URL'и історії покупок
- **Проблема:** JavaScript використовував неправильні URL з query параметрами
- **Рішення:** Змінено на правильні URL:
  ```javascript
  // Було: /api/purchases/history?customerId=123
  // Стало: /api/purchases/history/123
  fetch(`/api/purchases/history/${customerId}`)
  ```

#### 🚀 Додано версіонування JavaScript
- **Проблема:** Кеш браузера не оновлювався
- **Рішення:** Додано версії до всіх JS файлів `?v=1.1`

---

## 📊 API Документація (Повна)

### 🔐 Автентифікація `/api/auth`

#### POST `/api/auth/register` - Реєстрація
**Запит:**
```json
{
  "username": "newuser",
  "email": "user@example.com",
  "password": "securepassword"
}
```

**Відповідь (успіх):**
```json
{
  "success": true,
  "message": "Користувач успішно зареєстрований",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "username": "newuser",
    "role": "USER",
    "balance": 5000.0
  }
}
```

#### POST `/api/auth/login` - Вхід
**Запит:**
```json
{
  "username": "testuser",
  "password": "password123"
}
```

#### POST `/api/auth/refresh-token` - Оновлення токена
**Заголовок:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Відповідь:**
```json
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": { ... }
}
```

### 🛒 Товари `/api/products`

#### GET `/api/products` - Список всіх товарів
**Відповідь:**
```json
[
  {
    "id": 1,
    "name": "MacBook Pro 13",
    "price": 25000.0,
    "firm": "Apple",
    "quantity": 5,
    "maxDiscountPercentage": 10.0,
    "imageURL": "https://example.com/macbook.jpg",
    "type": "Laptop",
    "diagonal": 13.3,
    "weight": 1.4,
    "coreNumber": 8,
    "memory": 8192
  }
]
```

#### GET `/api/products/{id}` - Конкретний товар
#### GET `/api/products/search?name=MacBook` - Пошук товарів

#### GET `/api/products/calculate-price` - Розрахунок ціни з знижкою
**Параметри:** `?customerId=123&productId=456`
**Відповідь:**
```json
{
  "success": true,
  "originalPrice": 25000.0,
  "customerDiscount": 5.0,
  "maxProductDiscount": 10.0,
  "appliedDiscount": 5.0,
  "finalPrice": 23750.0,
  "savings": 1250.0
}
```

### 💳 Покупки `/api/purchases`

#### POST `/api/purchases` - Купити товар
**Параметри:** `?customerId=123&productId=456`
**Відповідь:**
```json
{
  "success": true,
  "message": "Покупка успішно завершена!",
  "productName": "MacBook Pro 13",
  "productFirm": "Apple",
  "originalPrice": 25000.0,
  "finalPrice": 23750.0,
  "appliedDiscount": 5.0,
  "remainingMoney": 21250.0
}
```

#### GET `/api/purchases/history/{customerId}` - Історія покупок
**Відповідь:**
```json
{
  "success": true,
  "purchases": [
    {
      "id": 1,
      "customerId": 123,
      "productId": 456,
      "productName": "MacBook Pro 13",
      "productFirm": "Apple",
      "originalPrice": 25000.0,
      "finalPrice": 23750.0,
      "appliedDiscount": 5.0,
      "purchaseDate": "2025-06-10T15:30:00"
    }
  ],
  "count": 1
}
```

#### GET `/api/purchases/statistics/{customerId}` - Статистика покупок
**Відповідь:**
```json
{
  "success": true,
  "statistics": {
    "totalSpent": 47500.0,
    "purchaseCount": 2,
    "averagePurchase": 23750.0,
    "topProducts": [
      ["MacBook Pro 13", 2],
      ["iPhone 14", 1]
    ]
  }
}
```

---

## 🎓 Поради для початківців

### 📚 Що вивчити далі?
1. **Spring Security** - розширена безпека
2. **Spring Data JPA** - складніші запити до БД
3. **Docker** - контейнеризація додатків
4. **React/Vue.js** - сучасний фронтенд
5. **Microservices** - архітектура великих систем

### 🛠️ Як розширити проект?
1. **Додати нові типи товарів** (телевізори, планшети)
2. **Реалізувати кошик покупок** (купівля кількох товарів одночасно)
3. **Додати систему коментарів та оцінок**
4. **Інтегрувати платіжні системи** (PayPal, Stripe)
5. **Додати email-повідомлення** про покупки

### 🔍 Корисні ресурси
- [Spring Boot Guide](https://spring.io/guides)
- [JPA Tutorial](https://www.baeldung.com/learn-jpa-hibernate)
- [JWT Explanation](https://jwt.io/introduction)
- [REST API Best Practices](https://restfulapi.net/)

---

## 📞 Підтримка та розвиток

Якщо у вас виникли питання або потрібна допомога:
1. Перевірте цю документацію - більшість відповідей тут
2. Подивіться секцію "Виправлення частих проблем"
3. Перевірте логи сервера в консолі
4. Використайте F12 в браузері для перевірки мережевих запитів

**Удачі у вивченні Spring Boot! 🚀** 