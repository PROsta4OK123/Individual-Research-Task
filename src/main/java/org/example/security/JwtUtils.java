package org.example.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {
    
    // Получаем секрет из переменных окружения или application.properties
    @Value("${app.jwt.secret:VoiceMoodSecretKeyForJWTTokenGenerationAndValidation2024SuperSecureDefaultKey}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration:86400000}")
    private int jwtExpirationMs; // 24 часа по умолчанию
    
    private SecretKey key;
    
    // Ленивая инициализация ключа
    private SecretKey getSigningKey() {
        if (key == null) {
            // Проверяем длину ключа для безопасности
            if (jwtSecret.length() < 64) {
                throw new IllegalArgumentException("JWT secret must be at least 64 characters for HS512 algorithm");
            }
            key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        }
        return key;
    }
    
    /**
     * Генерирует JWT токен для пользователя
     */
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
    
    /**
     * Извлекает имя пользователя из JWT токена
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    
    /**
     * Извлекает роль пользователя из JWT токена
     */
    public String getRoleFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }
    
    /**
     * Извлекает ID пользователя из JWT токена
     */
    public Long getUserIdFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userId", Long.class);
    }
    
    /**
     * Проверяет валидность JWT токена
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.err.println("JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("JWT claims string is empty: " + e.getMessage());
        }
        return false;
    }
} 