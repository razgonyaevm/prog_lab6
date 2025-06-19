package com.example.service;

import com.example.config.DatabaseConfig;
import com.example.service.enums.Color;
import com.example.service.enums.Country;
import com.example.service.enums.MovieGenre;
import com.example.service.enums.MpaaRating;
import com.example.service.model.Coordinates;
import com.example.service.model.Movie;
import com.example.service.model.Operator;
import com.example.service.model.User;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Класс для управления коллекцией с элементами {@link Movie} */
@Getter
public class MovieCollection {
  private static final Logger logger = LogManager.getLogger(MovieCollection.class);
  private final LocalDateTime initializationDate = LocalDateTime.now();

  private final List<Movie> movies = new LinkedList<>();
  private final ReentrantLock lock = new ReentrantLock();
  private final DatabaseConfig dbConfig;

  public MovieCollection(DatabaseConfig dbConfig) {
    this.dbConfig = dbConfig;
    loadFromDatabase();
  }

  /** Загрузка коллекции из базы данных */
  private void loadFromDatabase() {
    lock.lock();
    try (Connection conn =
        DriverManager.getConnection(
            dbConfig.getDbUrl(), dbConfig.getDbUser(), dbConfig.getDbPassword())) {
      String sql =
          "SELECT m.id, m.name, m.length, m.first_coordinate, m.second_coordinate, m.oscars_count, m.genre, "
              + "m.mpaa_rating, m.created_at, o.id as operator_id, o.name as operator_name, o.height, o.weight, "
              + "o.eye_color, o.country, u.id as user_id, u.login "
              + "FROM movies m "
              + "JOIN operators o ON m.operator_id = o.id "
              + "JOIN users u ON m.owner_id = u.id";
      try (Statement stmt = conn.createStatement();
          ResultSet rs = stmt.executeQuery(sql)) {
        movies.clear();
        while (rs.next()) {
          MovieGenre genre = null;
          MpaaRating mpaaRating = null;
          Long id = rs.getLong("id");
          String name = rs.getString("name");
          Coordinates coordinates =
              new Coordinates(rs.getDouble("first_coordinate"), rs.getLong("second_coordinate"));
          int oscarsCount = rs.getInt("oscars_count");
          Integer length = rs.getInt("length");
          if (rs.getString("genre") != null) {
            genre = MovieGenre.valueOf(rs.getString("genre").trim().toUpperCase());
          }
          if (rs.getString("mpaa_rating") != null) {
            mpaaRating = MpaaRating.valueOf(rs.getString("mpaa_rating").trim().toUpperCase());
          }

          Color color = null;
          Country country = null;
          if (rs.getString("eye_color") != null) {
            color = Color.valueOf(rs.getString("eye_color").trim().toUpperCase());
          }
          if (rs.getString("country") != null) {
            country = Country.valueOf(rs.getString("country").trim().toUpperCase());
          }
          Operator operator =
              new Operator(
                  rs.getString("operator_name"),
                  rs.getLong("height"),
                  rs.getFloat("weight"),
                  color,
                  country);
          operator.setId(rs.getLong("operator_id"));
          User owner = new User(rs.getInt("user_id"), rs.getString("login"));
          LocalDate createdAt = LocalDate.from(rs.getTimestamp("created_at").toLocalDateTime());
          Movie movie =
              new Movie(name, coordinates, oscarsCount, length, genre, mpaaRating, operator, owner);
          movie.setId(id);
          movie.setCreationDate(createdAt);
          movies.add(movie);
        }
        logger.info("Загружено {} фильмов из БД", movies.size());
      }
    } catch (SQLException e) {
      logger.error("Ошибка загрузки коллекции: {}", e.getMessage(), e);
    } finally {
      lock.unlock();
    }
  }

  /** Поиск или создание оператора в таблице operators */
  public Operator getOrCreateOperator(Operator operator) {
    lock.lock();
    try (Connection conn =
        DriverManager.getConnection(
            dbConfig.getDbUrl(), dbConfig.getDbUser(), dbConfig.getDbPassword())) {
      // Поиск существующего оператора
      String selectSql =
          "SELECT id FROM operators WHERE name = ? AND height = ? AND weight = ? "
              + "AND eye_color = ? AND country = ?";
      try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
        selectStmt.setString(1, operator.getName());
        selectStmt.setLong(2, operator.getHeight());
        selectStmt.setFloat(3, operator.getWeight());
        if (operator.getEyeColor() == null) {
          selectStmt.setNull(4, Types.CHAR);
        } else {
          selectStmt.setString(4, operator.getEyeColor().toString().trim().toUpperCase());
        }
        if (operator.getNationality() == null) {
          selectStmt.setNull(5, Types.VARCHAR);
        } else {
          selectStmt.setString(5, operator.getNationality().toString().trim().toUpperCase());
        }
        ResultSet rs = selectStmt.executeQuery();
        if (rs.next()) {
          operator.setId(rs.getLong("id"));
          return operator;
        }
      }
      // Создание нового оператора
      String insertSql =
          "INSERT INTO operators (name, height, weight, eye_color, country) "
              + "VALUES (?, ?, ?, ?, ?) RETURNING id";
      try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
        insertStmt.setString(1, operator.getName());
        insertStmt.setLong(2, operator.getHeight());
        insertStmt.setFloat(3, operator.getWeight());
        if (operator.getEyeColor() == null) {
          insertStmt.setNull(4, Types.CHAR);
        } else {
          insertStmt.setString(4, operator.getEyeColor().toString().trim().toUpperCase());
        }
        if (operator.getNationality() == null) {
          insertStmt.setNull(5, Types.VARCHAR);
        } else {
          insertStmt.setString(5, operator.getNationality().toString().trim().toUpperCase());
        }
        ResultSet rs = insertStmt.executeQuery();
        if (rs.next()) {
          long id = rs.getLong("id");
          operator.setId(id);
          logger.info("Создан оператор: {} с id {}", operator.getName(), id);
          return operator;
        }
      }
      throw new SQLException("Не удалось создать оператора");
    } catch (SQLException e) {
      logger.error(
          "Ошибка поиска или создания оператора {}: {}", operator.getName(), e.getMessage(), e);
      throw new RuntimeException("Ошибка создания оператора", e);
    } finally {
      lock.unlock();
    }
  }

  /** Добавляет элемент в коллекцию */
  public boolean add(Movie movie, User owner) {
    lock.lock();
    try (Connection conn =
        DriverManager.getConnection(
            dbConfig.getDbUrl(), dbConfig.getDbUser(), dbConfig.getDbPassword())) {
      conn.setAutoCommit(false);
      Operator operator = getOrCreateOperator(movie.getOperator());
      movie.setOperator(operator);

      String sql =
          "INSERT INTO movies (name, length, first_coordinate, second_coordinate, oscars_count, genre, "
              + "mpaa_rating, operator_id, owner_id, created_at) "
              + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
      try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, movie.getName());
        pstmt.setInt(2, movie.getLength());
        pstmt.setDouble(3, movie.getCoordinates().getX());
        pstmt.setLong(4, movie.getCoordinates().getY());
        pstmt.setInt(5, movie.getOscarsCount());
        if (movie.getGenre() == null) {
          pstmt.setNull(6, Types.CHAR);
        } else {
          pstmt.setString(6, movie.getGenre().toString().trim().toUpperCase());
        }
        if (movie.getMpaaRating() == null) {
          pstmt.setNull(7, Types.CHAR);
        } else {
          pstmt.setString(7, movie.getMpaaRating().toString().trim().toUpperCase());
        }
        pstmt.setLong(8, movie.getOperator().getId());
        pstmt.setInt(9, owner.getId());
        pstmt.setTimestamp(10, Timestamp.valueOf(movie.getCreationDate().atStartOfDay()));
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
          movie.setId(rs.getLong("id"));
          movie.setOwner(owner);
          movies.add(movie);
          conn.commit();
          logger.info("Фильм {} добавлен в БД и в коллекцию", movie.getName());
          return true;
        }
        conn.rollback();
        logger.error("Ошибка добавления фильма в БД");
        return false;
      }
    } catch (SQLException e) {
      logger.error("Ошибка добавления фильма в БД: {}", e.getMessage(), e);
      return false;
    } finally {
      lock.unlock();
    }
  }

  /** Устанавливает новый элемент на место с индексом id */
  public boolean update(long id, Movie movie, User owner) {
    lock.lock();
    try (Connection conn =
        DriverManager.getConnection(
            dbConfig.getDbUrl(), dbConfig.getDbUser(), dbConfig.getDbPassword())) {
      conn.setAutoCommit(false);
      Operator operator = getOrCreateOperator(movie.getOperator());
      movie.setOperator(operator);

      String sql =
          "UPDATE movies "
              + "SET id = ?, name = ?, length = ?, first_coordinate = ?, second_coordinate = ?, "
              + "oscars_count = ?, genre = ?, mpaa_rating = ?, operator_id = ?, owner_id = ?, created_at = ? "
              + "WHERE id = ? AND owner_id = ?";
      try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setLong(1, id);
        pstmt.setString(2, movie.getName());
        pstmt.setInt(3, movie.getLength());
        pstmt.setDouble(4, movie.getCoordinates().getX());
        pstmt.setLong(5, movie.getCoordinates().getY());
        pstmt.setInt(6, movie.getOscarsCount());
        if (movie.getGenre() == null) {
          pstmt.setNull(7, Types.CHAR);
        } else {
          pstmt.setString(7, movie.getGenre().toString().trim().toUpperCase());
        }
        if (movie.getMpaaRating() == null) {
          pstmt.setNull(8, Types.CHAR);
        } else {
          pstmt.setString(8, movie.getMpaaRating().toString().trim().toUpperCase());
        }
        pstmt.setLong(9, movie.getOperator().getId());
        pstmt.setInt(10, owner.getId());
        pstmt.setTimestamp(11, Timestamp.valueOf(movie.getCreationDate().atStartOfDay()));
        pstmt.setLong(12, id);
        pstmt.setInt(13, owner.getId());
        int rows = pstmt.executeUpdate();
        if (rows > 0) {
          movie.setId(id);
          movie.setOwner(owner);
          movies.removeIf(m -> m.getId() == id);
          movies.add(movie);
          conn.commit();
          logger.info("Фильм с id {} обновлен в БД и в коллекции", id);
          return true;
        }
        conn.rollback();
        logger.error("Ошибка обновления фильма в БД");
        return false;
      }
    } catch (SQLException e) {
      logger.error("Ошибка обновления фильма в БД: {}", e.getMessage(), e);
      return false;
    } finally {
      lock.unlock();
    }
  }

  /** Очищает коллекцию */
  public boolean clear(User owner) {
    lock.lock();
    try (Connection conn =
        DriverManager.getConnection(
            dbConfig.getDbUrl(), dbConfig.getDbUser(), dbConfig.getDbPassword())) {
      conn.setAutoCommit(false);
      String sql = "DELETE FROM movies WHERE owner_id = ?";
      try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, owner.getId());
        int rows = pstmt.executeUpdate();
        movies.removeIf(movie -> movie.getOwner().getId() == owner.getId());
        conn.commit();
        logger.info("Удалено {} фильмов пользователя {}", rows, owner.getLogin());
        return rows > 0;
      }
    } catch (SQLException e) {
      logger.error("Ошибка очистки коллекции: {}", e.getMessage(), e);
      return false;
    } finally {
      lock.unlock();
    }
  }

  /** Удаляет элемент по значению его id */
  public boolean removeById(long id, User owner) {
    lock.lock();
    try (Connection conn =
        DriverManager.getConnection(
            dbConfig.getDbUrl(), dbConfig.getDbUser(), dbConfig.getDbPassword())) {
      conn.setAutoCommit(false);
      String sql = "DELETE FROM movies WHERE id = ? AND owner_id = ? RETURNING id";
      try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setLong(1, id);
        pstmt.setInt(2, owner.getId());
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
          movies.removeIf(m -> m.getId() == id);
          conn.commit();
          logger.info(
              "Фильм с id {} удален из БД и из коллекции пользователя {}", id, owner.getLogin());
          return true;
        }
        conn.rollback();
        logger.warn("Фильм с id {} не удален: не найден или доступ запрещен", id);
        return false;
      }
    } catch (SQLException e) {
      logger.error("Ошибка удаления фильма с ID {}: {}", id, e.getMessage(), e);
      return false;
    } finally {
      lock.unlock();
    }
  }

  /** Выводит все элементы коллекции в строковом представлении */
  public String show() {
    lock.lock();
    try {
      int index = 1; // Нумерация начинается с 1
      StringBuilder result = new StringBuilder();
      for (Movie movie : movies) {
        result.append("Фильм ").append(index).append(": ").append(movie).append("\n");
        index++;
      }
      if (movies.isEmpty()) {
        return "В коллекции нет элементов";
      } else {
        return result.toString();
      }
    } finally {
      lock.unlock();
    }
  }

  /** Меняет порядок элементов коллекции на противоположный */
  public void reorder() {
    lock.lock();
    try {
      Collections.reverse(movies);
    } finally {
      lock.unlock();
    }
  }

  /** Удаляет элемент коллекции по индексу */
  public boolean removeAt(int index, User owner) {
    lock.lock();
    try (Connection conn =
        DriverManager.getConnection(
            dbConfig.getDbUrl(), dbConfig.getDbUser(), dbConfig.getDbPassword())) {
      conn.setAutoCommit(false);
      if (index < 0 || index >= movies.size()) {
        logger.warn("Некорректный индекс: {}", index);
        return false;
      }
      Movie movie = movies.get(index);
      if (movie.getOwner().getId() != owner.getId()) {
        logger.warn(
            "Попытка удаления фильма с индексом {} не принадлежащего пользователю {}",
            index,
            owner.getLogin());
        return false;
      }
      String sql = "DELETE FROM movies WHERE id = ? AND owner_id = ? RETURNING id";
      try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setLong(1, movie.getId());
        pstmt.setInt(2, owner.getId());
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
          movies.remove(index);
          conn.commit();
          logger.info(
              "Фильм с id {} удален из БД и из коллекции пользователя {}",
              movie.getId(),
              owner.getLogin());
          return true;
        }
        conn.rollback();
        logger.warn("Фильм с id {} не удален: не найден или доступ запрещен", movie.getId());
        return false;
      }
    } catch (SQLException e) {
      logger.error("Ошибка удаления фильма с индексом {}: {}", index, e.getMessage(), e);
      return false;
    } finally {
      lock.unlock();
    }
  }

  /** Удаляет первый элемент коллекции */
  public boolean removeFirst(User owner) {
    lock.lock();
    try (Connection conn =
        DriverManager.getConnection(
            dbConfig.getDbUrl(), dbConfig.getDbUser(), dbConfig.getDbPassword())) {
      conn.setAutoCommit(false);
      String sql =
          "DELETE FROM movies WHERE id = (SELECT MIN(id) FROM movies WHERE owner_id = ?) RETURNING id";
      try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, owner.getId());
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
          long id = rs.getLong("id");
          movies.removeIf(m -> m.getId() == id);
          conn.commit();
          logger.info(
              "Фильм с id {} удален из БД и из коллекции пользователя {}", id, owner.getLogin());
          return true;
        }
        conn.rollback();
        logger.error("Ошибка удаления фильма из БД");
        return false;
      }
    } catch (SQLException e) {
      logger.error("Ошибка удаления фильма из БД: {}", e.getMessage(), e);
      return false;
    } finally {
      lock.unlock();
    }
  }

  /** Выводит сумму значений поля length для всех элементов коллекции */
  public String sumOfLength() {
    lock.lock();
    try {
      int sum = movies.stream().mapToInt(Movie::getLength).sum();
      return "Сумма length: " + sum;
    } finally {
      lock.unlock();
    }
  }

  /** Выводит количество элементов, у которых имя оператора равно заданному */
  public String countByOperator(String operatorName) {
    lock.lock();
    try {
      long count =
          movies.stream()
              .filter(m -> Objects.equals(m.getOperator().getName(), operatorName))
              .count();
      return "Количество фильмов оператора " + operatorName + ": " + count;
    } finally {
      lock.unlock();
    }
  }

  /** Выводит количество оскаров у всех фильмов в порядке убывания */
  public String printDescendingOscarsCount() {
    lock.lock();
    try {
      String result =
          movies.stream()
              .map(Movie::getOscarsCount)
              .sorted(Comparator.reverseOrder())
              .map(String::valueOf)
              .collect(Collectors.joining(", "));
      return result.isEmpty() ? "Коллекция пуста" : result;
    } finally {
      lock.unlock();
    }
  }

  /** Возвращает размер коллекции */
  public int size() {
    lock.lock();
    try {
      return movies.size();
    } finally {
      lock.unlock();
    }
  }

  /** Возвращает элемент коллекции по индексу */
  public Movie get(int index) {
    lock.lock();
    try {
      return movies.get(index);
    } finally {
      lock.unlock();
    }
  }
}
