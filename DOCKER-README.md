# 🐳 VoiceMood Docker Setup

Цей документ описує, як запустити VoiceMood систему покупок у Docker контейнері.

## 📋 Вимоги

- Docker 20.10+
- Docker Compose 1.29+

## 🚀 Швидкий запуск

### Використання Docker Compose (рекомендовано)

```bash
# Збудувати та запустити контейнер
docker-compose up --build

# Запустити у фоновому режимі
docker-compose up --build -d

# Зупинити контейнер
docker-compose down
```

### Використання Docker напряму

```bash
# Збудувати образ
docker build -t voicemood-app .

# Запустити контейнер
docker run -p 8080:8080 --name voicemood-container voicemood-app

# Зупинити контейнер
docker stop voicemood-container

# Видалити контейнер
docker rm voicemood-container
```

## 🌐 Доступ до додатку

Після успішного запуску додаток буде доступний за адресою:
- **Основний додаток**: http://localhost:8080
- **H2 консоль** (для налагодження): http://localhost:8080/h2-console

### Налаштування H2 консолі:
- **JDBC URL**: `jdbc:h2:mem:voicemood`
- **User Name**: `sa`
- **Password**: (залишити порожнім)

## 🔧 Налаштування

### Профілі Spring Boot

Додаток використовує різні профілі:
- **default**: Використовує MySQL (для локальної розробки)
- **docker**: Використовує H2 in-memory базу даних (для Docker)

### Змінні середовища

У `docker-compose.yml` можна налаштувати:

```yaml
environment:
  - SPRING_PROFILES_ACTIVE=docker
  - SERVER_PORT=8080
  - LOGGING_LEVEL_ROOT=INFO
```

## 📁 Структура проекту

```
.
├── Dockerfile              # Основний Dockerfile
├── docker-compose.yml      # Docker Compose конфігурація
├── .dockerignore           # Файли, що виключаються з контексту збірки
├── src/
│   └── main/
│       └── resources/
│           ├── application.properties         # Основні налаштування
│           └── application-docker.properties  # Налаштування для Docker
└── target/                 # Збудований JAR файл
```

## 🛠️ Розробка

### Перебудова образу

```bash
# При зміні коду
docker-compose up --build

# Примусова перебудова без кешу
docker-compose build --no-cache
docker-compose up
```

### Перегляд логів

```bash
# Всі логи
docker-compose logs

# Логи у реальному часі
docker-compose logs -f

# Логи конкретного сервісу
docker-compose logs voicemood-app
```

### Виконання команд у контейнері

```bash
# Вхід у контейнер
docker-compose exec voicemood-app bash

# Або якщо bash недоступний
docker-compose exec voicemood-app sh
```

## 🔍 Відлагодження

### Перевірка статусу контейнера

```bash
# Список запущених контейнерів
docker ps

# Детальна інформація
docker-compose ps
```

### Очистка

```bash
# Зупинити та видалити контейнери
docker-compose down

# Видалити образи
docker rmi voicemood-app

# Очистити все (обережно!)
docker system prune -f
```

## 📊 Моніторинг

### Використання ресурсів

```bash
# Статистика контейнера
docker stats voicemood-container

# Або через compose
docker-compose top
```

## 🔐 Безпека

У production середовищі рекомендується:

1. Використовувати multi-stage Dockerfile
2. Не зберігати паролі у коді
3. Використовувати Docker secrets
4. Оновлювати базові образи

## 📝 Примітки

- H2 база даних працює у режимі in-memory, тому дані не зберігаються після перезапуску
- Для production варто використовувати зовнішню базу даних (PostgreSQL, MySQL)
- Логи за замовчуванням виводяться у stdout контейнера 