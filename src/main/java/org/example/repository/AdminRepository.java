package org.example.repository;

import org.example.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    
    // Поиск администратора по имени пользователя
    Optional<Admin> findByUsername(String username);
    
    // Поиск администратора по email
    Optional<Admin> findByEmail(String email);
    
    // Поиск администраторов по уровню доступа
    List<Admin> findByAdminLevel(int adminLevel);
    
    // Поиск администраторов с определенными правами
    @Query("SELECT a FROM Admin a WHERE a.permissions LIKE %:permission%")
    List<Admin> findByPermission(@Param("permission") String permission);
    
    // Поиск администраторов, которые входили после определенной даты
    List<Admin> findByLastLoginAfter(LocalDateTime date);
    
    // Проверка существования администратора по username или email
    boolean existsByUsernameOrEmail(String username, String email);
} 