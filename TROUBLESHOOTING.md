# 🔧 Виправлення проблем VoiceMood
## Детальний посібник для початківців

> **🎯 Цей файл допоможе вам швидко знайти та виправити найпоширені проблеми в проекті.**

---

## 🚨 Екстрені виправлення

### ❌ Помилка: "Cannot read properties of undefined (reading 'toFixed')"

**🔍 Симптоми:**
- Помилка в консолі браузера при спробі купити товар
- Покупка проходить успішно на сервері, але інтерфейс показує помилку

**🧐 Причина:**
JavaScript очікує числові поля (`finalPrice`, `remainingMoney`) у відповіді сервера, але контролер їх не повертає або повертає в іншому форматі.

**✅ Рішення покроково:**

1. **Перевірте PurchaseController.java:**
   ```java
   // У методі makePurchase() має бути:
   return ResponseEntity.ok(Map.of(
       "success", true,
       "message", "Покупка успішно завершена!",
       "productName", product.getName(),        // ← Обов'язково
       "finalPrice", finalPrice,                // ← Обов'язково число, фронтенд очікує його
       "appliedDiscount", appliedDiscount,      // ← Обов'язково число, фронтенд очікує його
       "remainingMoney", customerAfter.getMoney() // ← Обов'язково число, фронтенд очікує його
   ));
   ```

2. **Перевірте JavaScript (purchase.js):**
   ```javascript
   // Додайте перевірки перед використанням toFixed():
   if (data.finalPrice !== undefined && data.finalPrice !== null) {
       const priceText = data.finalPrice.toFixed(2);
   }
   ```

3. **Перезапустіть сервер:**
   ```bash
   mvn spring-boot:run
   ```

---

### ❌ Помилка: "Не вдалося завантажити історію покупок"

**🔍 Симптоми:**
- На вкладці "Історія" з'являється червоне повідомлення про помилку
- В логах сервера: 404 NOT FOUND для `/api/purchases/history`

**🧐 Причина:**
JavaScript робить запити з неправильними URL'ами (використовує query параметри замість path параметрів).

**✅ Рішення покроково:**

1. **Перевірте history.js - правильні URL:**
   ```javascript
   // ✅ ПРАВИЛЬНО:
   fetch(`/api/purchases/history/${customerId}`)
   fetch(`/api/purchases/statistics/${customerId}`)
   
   // ❌ НЕПРАВИЛЬНО:
   fetch(`/api/purchases/history?customerId=${customerId}`)
   ```

2. **Перевірте обробку відповідей:**
   ```javascript
   .then(data => {
       if (data.success) {
           displayPurchaseHistory(data.purchases);  // ← data.purchases, не просто data
       } else {
           throw new Error(data.message);
       }
   })
   ```

3. **Очистіть кеш браузера:** `Ctrl+Shift+R`

---

### ❌ Помилка: 404 NOT FOUND для API endpoints

**🔍 Симптоми:**
- Запити на `/api/purchases`, `/api/products` повертають 404
- Сервер запущений, але API не працює

**🧐 Причина:**
Контролер не реєструється Spring'ом або неправильні анотації.

**✅ Рішення покроково:**

1. **Перевірте анотації контролера:**
   ```java
   @RestController                    // ← Обов'язково
   @RequestMapping("/api/purchases")  // ← Базовий шлях
   @CrossOrigin(origins = "*")        // ← Для CORS
   public class PurchaseController {
       
       @PostMapping  // ← Для POST /api/purchases
       public ResponseEntity<Map<String, Object>> makePurchase(...) {
   ```

2. **Перевірте, що клас знаходиться в правильному пакеті:**
   ```
   src/main/java/org/example/controller/PurchaseController.java
   ```

3. **Перевірте логи запуску сервера:**
   Має бути щось на кшталт:
   ```
   Mapped "{[/api/purchases],methods=[POST]}" onto public org.springframework.http.ResponseEntity...
   ```

---

## 🌐 Проблеми з фронтендом

### ❌ JavaScript файли не завантажуються (кеш)

**🔍 Симптоми:**
- Зміни в JavaScript не застосовуються
- Старі помилки повторюються після виправлення

**✅ Рішення:**

1. **Додайте версії до файлів (index.html):**
   ```html
   <script src="js/app.js?v=1.1"></script>
   <script src="js/history.js?v=1.1"></script>
   <script src="js/purchase.js?v=1.1"></script>
   ```

2. **Жорстке перезавантаження:**
   - Chrome/Edge: `Ctrl+Shift+R`
   - Firefox: `Ctrl+F5`
   - Safari: `Cmd+Shift+R`

3. **Очищення кешу через DevTools:**
   - F12 → Network tab → ✅ "Disable cache"
   - Або F12 → Application → Storage → Clear storage

---

### ❌ CORS помилки

**🔍 Симптоми:**
```
Access to fetch at 'http://localhost:8080/api/...' from origin 'null' has been blocked by CORS policy
```

**✅ Рішення:**
Додайте `@CrossOrigin` до всіх контролерів:
```java
@RestController
@CrossOrigin(origins = "*")  // ← Додати цю анотацію
public class YourController {
```

---

## 🗄️ Проблеми з базою даних

### ❌ Помилка підключення до MySQL

**🔍 Симптоми:**
```
com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
```

**✅ Рішення покроково:**

1. **Перевірте, що MySQL запущений:**
   ```bash
   # Windows
   net start mysql80
   
   # macOS/Linux
   sudo systemctl start mysql
   ```

2. **Перевірте налаштування (application.properties):**
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/voicemood_db
   spring.datasource.username=voicemood_user
   spring.datasource.password=password123
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
   ```

3. **Створіть базу даних вручну:**
   ```sql
   CREATE DATABASE voicemood_db;
   CREATE USER 'voicemood_user'@'localhost' IDENTIFIED BY 'password123';
   GRANT ALL PRIVILEGES ON voicemood_db.* TO 'voicemood_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

---

### ❌ Таблиці не створюються автоматично

**✅ Рішення:**
Додайте до application.properties:
```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```

---

## 🔐 Проблеми з JWT автентифікацією

### ❌ Токени швидко закінчуються

**✅ Налаштування часу життя:**
```properties
jwt.expiration=1200000  # 20 хвилин (у мілісекундах)
```

### ❌ Автоматичне оновлення не працює

**✅ Перевірте JavaScript:**
```javascript
// Має бути в app.js
setInterval(async function() {
    if (currentUser && localStorage.getItem('authToken')) {
        await refreshTokenIfNeeded();
    }
}, 2 * 60 * 1000); // Кожні 2 хвилини
```

---

## 🚀 Проблеми з запуском

### ❌ Порт 8080 зайнятий

**🔍 Симптоми:**
```
Port 8080 was already in use.
```

**✅ Рішення:**

1. **Знайти процес, що використовує порт:**
   ```bash
   # Windows
   netstat -ano | findstr :8080
   
   # macOS/Linux
   lsof -i :8080
   ```

2. **Вбити процес або змінити порт:**
   ```properties
   # application.properties
   server.port=8081
   ```

---

### ❌ Maven помилки збірки

**🔍 Симптоми:**
```
Failed to execute goal org.springframework.boot:spring-boot-maven-plugin
```

**✅ Рішення:**

1. **Очистіть проект:**
   ```bash
   mvn clean
   mvn install
   ```

2. **Перевірте Java версію:**
   ```bash
   java -version  # Має бути 17+
   ```

3. **Оновіть залежності:**
   ```bash
   mvn dependency:resolve
   ```

---

## 🔍 Діагностичні команди

### 📊 Перевірка стану системи

```bash
# 1. Перевірка запущених Java процесів
jps -l

# 2. Перевірка портів
netstat -tulpn | grep 8080

# 3. Перевірка логів Spring Boot
tail -f logs/spring.log

# 4. Тест підключення до БД
mysql -h localhost -u voicemood_user -p voicemood_db
```

### 🌐 Тестування API вручну

```bash
# Тест реєстрації
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"password123"}'

# Тест входу
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'

# Тест списку товарів
curl http://localhost:8080/api/products
```

---

## 📋 Чек-лист перед зверненням за допомогою

Перед тим, як шукати допомогу, перевірте:

- [ ] ✅ Сервер запущений (`mvn spring-boot:run`)
- [ ] ✅ MySQL працює та база даних створена
- [ ] ✅ Немає помилок у логах сервера
- [ ] ✅ Кеш браузера очищений (`Ctrl+Shift+R`)
- [ ] ✅ JavaScript файли завантажуються (F12 → Network)
- [ ] ✅ Правильні URL'и в JavaScript коді
- [ ] ✅ API endpoints повертають правильні дані (F12 → Network → відповіді)

---

## 🆘 Швидке відновлення після критичних помилок

Якщо все зламалось:

1. **Зупинити сервер:** `Ctrl+C`
2. **Очистити проект:** `mvn clean`
3. **Пересоздати базу даних:**
   ```sql
   DROP DATABASE voicemood_db;
   CREATE DATABASE voicemood_db;
   ```
4. **Запустити заново:** `mvn spring-boot:run`
5. **Очистити кеш браузера:** `Ctrl+Shift+R`

**🎯 Якщо і це не допомогло - перевірте цей файл спочатку!** 