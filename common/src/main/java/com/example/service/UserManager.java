package com.example.service;

import com.example.config.DatabaseConfig;
import com.example.service.model.User;
import java.security.MessageDigest;
import java.sql.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserManager {
  private static final Logger logger = LogManager.getLogger(UserManager.class);
  private final DatabaseConfig dbConfig;
  private final ConcurrentHashMap<String, User> authenticatedUsers = new ConcurrentHashMap<>();
  private final ReentrantLock lock = new ReentrantLock();
  @Getter private User currentUser;

  public UserManager(DatabaseConfig dbConfig) {
    this.dbConfig = dbConfig;
  }

  /** Регистрация пользователя */
  public User register(String login, String password) {
    lock.lock();
    try (Connection conn =
        DriverManager.getConnection(
            dbConfig.getDbUrl(), dbConfig.getDbUser(), dbConfig.getDbPassword())) {
      String sql = "INSERT INTO users (login, password) VALUES (?, ?) RETURNING id";
      try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, login);
        pstmt.setString(2, hashPassword(password));
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
          int id = rs.getInt("id");
          User user = new User(id, login);
          logger.info("Пользователь {} зарегистрирован", login);
          currentUser = user;
          return user;
        }
        return null;
      }
    } catch (SQLException e) {
      logger.error("Ошибка регистрации пользователя {}: {}", login, e.getMessage());
      return null;
    } finally {
      lock.unlock();
    }
  }

  /** Авторизация пользователя */
  public User login(String login, String password) {
    lock.lock();
    try (Connection conn =
        DriverManager.getConnection(
            dbConfig.getDbUrl(), dbConfig.getDbUser(), dbConfig.getDbPassword())) {
      String sql = "SELECT id, login FROM users WHERE login = ? AND password = ?";
      try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, login);
        pstmt.setString(2, hashPassword(password));
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
          User user = new User(rs.getInt("id"), rs.getString("login"));
          authenticatedUsers.put(login, user);
          logger.info("Пользователь {} авторизован", login);
          currentUser = user;
          return user;
        }
        return null;
      }
    } catch (SQLException e) {
      logger.error("Ошибка авторизации пользователя {}: {}", login, e.getMessage());
      return null;
    } finally {
      lock.unlock();
    }
  }

  /** Проверка авторизации */
  public User verify(String login, String password) {
    if (login == null || password == null) {
      return null;
    }
    try (Connection conn =
        DriverManager.getConnection(
            dbConfig.getDbUrl(), dbConfig.getDbUser(), dbConfig.getDbPassword())) {
      String sql = "SELECT id, login FROM users WHERE login = ? AND password = ?";
      try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, login);
        pstmt.setString(2, hashPassword(password));
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
          User user = new User(rs.getInt("id"), rs.getString("login"));
          currentUser = user;
          return user;
        }
      }
    } catch (SQLException e) {
      logger.error("Ошибка верификации пользователя {}: {}", login, e.getMessage());
    }
    return null;
  }

  /** Хэширование пароля MD5 */
  private String hashPassword(String password) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] hash = md.digest(password.getBytes());
      StringBuilder sb = new StringBuilder();
      for (byte b : hash) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      logger.error("Ошибка хэширования пароля: {}", e.getMessage());
      return null;
    }
  }

  /** Проверка пароля */
  private boolean validatePassword(String login, String password) {
    try (Connection conn =
        DriverManager.getConnection(
            dbConfig.getDbUrl(), dbConfig.getDbUser(), dbConfig.getDbPassword())) {
      String sql = "SELECT password FROM users WHERE login = ?";
      try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, login);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
          return rs.getString("password").equals(hashPassword(password));
        }
      }
    } catch (SQLException e) {
      logger.error("Ошибка проверки пароля для {}: {}", login, e.getMessage());
    }
    return false;
  }
}
