package com.example.app.commands;

import com.example.app.Command;
import com.example.network.Response;
import com.example.service.MovieCollection;
import com.example.service.UserManager;
import com.example.service.model.Movie;
import com.example.service.model.User;
import java.io.Serializable;
import lombok.Getter;

/** Команда для добавления нового фильма в коллекцию */
public class AddCommand implements Command, Serializable {
  private final MovieCollection collection; // Коллекция фильмов
  @Getter private final Movie movie; // Фильм для добавления
  private final String login;
  private final String password;

  /**
   * Конструктор команды добавления
   *
   * @param collection Коллекция, в которую добавляется фильм
   * @param movie Фильм для добавления
   */
  public AddCommand(MovieCollection collection, Movie movie, String login, String password) {
    this.collection = collection;
    this.movie = movie;
    this.login = login;
    this.password = password;
  }

  /**
   * Выполняет команду добавления
   *
   * @return Ответ с результатом операции
   */
  @Override
  public Response execute(UserManager userManager) {
    User user = userManager.verify(login, password);
    if (user == null) {
      return new Response("Неавторизованный доступ", false);
    }
    try {
      if (movie == null) {
        return new Response("Ошибка: данные фильма не предоставлены", false);
      }
      movie.generateId();
      if (collection.add(movie, user)) {
        return new Response("Фильм успешно добавлен", true);
      } else throw new IllegalArgumentException("Ошибка добавления фильма");
    } catch (Exception e) {
      return new Response("Ошибка при добавлении фильма: " + e.getMessage(), false);
    }
  }
}
