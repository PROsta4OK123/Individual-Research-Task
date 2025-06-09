# Этап сборки с Maven
FROM maven:3.8.4-openjdk-17 AS build

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файлы Maven для кеширования зависимостей
COPY pom.xml .

# Загружаем зависимости (это будет закешировано, если pom.xml не изменяется)
RUN mvn dependency:resolve

# Копируем весь исходный код
COPY src ./src

# Собираем проект
RUN mvn clean package -DskipTests

# Этап выполнения
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем собранный JAR файл из этапа сборки
COPY --from=build /app/target/Kursovaya-1.0-SNAPSHOT.jar app.jar

# Экспонируем порт 8080
EXPOSE 8080

# Команда для запуска приложения
CMD ["java", "-jar", "app.jar"] 