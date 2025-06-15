# 🚀 Швидкий старт VoiceMood

## ⚡ Запуск за 5 хвилин

### 1. Передумови
```bash
# Перевірте наявність необхідних інструментів
java --version    # Java 17+
mvn --version     # Maven 3.6+
mysql --version   # MySQL 8.0+
```

### 2. Налаштування бази даних
```sql
-- Увійдіть в MySQL як root
mysql -u root -p

-- Створіть базу та користувача
CREATE DATABASE voicemood_db;
CREATE USER 'voicemood_user'@'localhost' IDENTIFIED BY 'password123';
GRANT ALL PRIVILEGES ON voicemood_db.* TO 'voicemood_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 3. Запуск проекту
```bash
# Клонуйте проект (або перейдіть в директорію)
cd VoiceMood

# Запустіть додаток
mvn spring-boot:run
```

### 4. Перевірка роботи
- 🌐 Відкрийте: http://localhost:8080
- 👤 Увійдіть як `testuser` / `password123`
- 👨‍💼 Або як адмін: `admin` / `admin123`

---

## 🎯 Швидкі тести

### Тестування JWT
```bash
# Відкрийте test_jwt.html у браузері
open test_jwt.html
```

### API тестування
```bash
# Перевірка статусу сервера
curl http://localhost:8080/api/products

# Авторизація
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

---

## ⚙️ Основні функції

### Для користувачів:
1. 📝 Реєстрація/Вхід
2. 🛒 Перегляд каталогу
3. 💳 Здійснення покупок
4. 📊 Історія покупок

### Для адмінів:
1. ➕ Створення товарів
2. 📦 Управління інвентарем
3. 👥 Управління користувачами
4. 📈 Перегляд статистики

---

## 🔧 Налаштування (опціонально)

### Зміна часу життя JWT токена
```properties
# application.properties
app.jwt.expiration=1200000  # 20 хвилин (за замовчуванням)
```

### Зміна порту сервера
```properties
# application.properties
server.port=8081  # Замість 8080
```

### Налаштування логування
```properties
# application.properties
logging.level.org.springframework.web=INFO  # Менше логів
```

---

## 🆘 Вирішення проблем

### ❌ "Помилка завантаження історії покупок"
```bash
# 1. Очистіть кеш браузера
Ctrl+Shift+R

# 2. Перевірте, що сервер запущений
mvn spring-boot:run
```

### ❌ JavaScript помилки з toFixed()
**Рішення:** Перезапустіть сервер і очистіть кеш браузера.
Контролери були оновлені і тепер повертають правильні дані.

### ❌ Проблема з базою даних
```bash
# Перевірте статус MySQL
sudo systemctl status mysql

# Перезапустіть MySQL
sudo systemctl restart mysql
```

### ❌ Проблема з портом 8080
```bash
# Перевірте який процес використовує порт
lsof -i :8080

# Або змініть порт в application.properties
server.port=8081
```

### ❌ Проблема з Maven
```bash
# Очистіть кеш Maven
mvn clean

# Переустановіть залежності
mvn clean install
```

### 📋 Детальна діагностика
➡️ **Для складних проблем дивіться:** [TROUBLESHOOTING.md](TROUBLESHOOTING.md)

---

## 📚 Корисні посилання

- 📖 **Повна документація:** [README.md](README.md)
- 🔧 **Технічний посібник:** [DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md) 
- 🔄 **JWT документація:** [TOKEN_REFRESH_INFO.md](TOKEN_REFRESH_INFO.md)
- 🧪 **Тестування JWT:** [test_jwt.html](test_jwt.html)

---

## 🎉 Готово!

Ваш VoiceMood додаток готовий до роботи! 

**Адреса:** http://localhost:8080  
**Тестові акаунти готові для використання!** 🚀 