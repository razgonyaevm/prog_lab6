package com.example.app.commands;

import static com.example.parsing.ParserClass.parseLong;

import com.example.app.Command;
import com.example.network.Response;
import com.example.parsing.ScanMovie;
import com.example.service.MovieCollection;
import com.example.service.UserManager;
import com.example.service.model.Movie;
import com.example.service.model.User;
import java.io.Serializable;
import java.util.Scanner;
import lombok.Getter;

/** Обновление элемента в коллекции по его id */
public class UpdateCommand implements Command, Serializable {
  private final MovieCollection collection;
  @Getter private Movie movie;
  @Getter private final String command;
  private final boolean executeScript;
  private final Scanner scanner;
  @Getter private final String login;
  @Getter private final String password;

  public UpdateCommand(
      MovieCollection collection, Movie movie, String command, String login, String password) {
    this.collection = collection;
    this.movie = movie;
    this.command = command;
    this.scanner = null;
    this.executeScript = false;
    this.login = login;
    this.password = password;
  }

  public UpdateCommand(
      MovieCollection collection,
      Scanner scanner,
      String command,
      Boolean executeScript,
      String login,
      String password) {
    this.collection = collection;
    this.scanner = scanner;
    this.executeScript = executeScript;
    this.command = command;
    this.movie = null;
    this.login = login;
    this.password = password;
  }

  @Override
  public Response execute(UserManager userManager) {
    User user = userManager.verify(login, password);
    if (user == null) {
      return new Response("Неавторизованный доступ", false);
    }
    String[] parts = command.trim().split("\\s+");
    if (parts.length != 2) {
      return new Response("Ошибка: укажите id", false);
    }
    try {
      long id = parseLong(parts[1]);
      if (id <= 0) {
        return new Response("ID must be greater than 0", false);
      }
      for (int i = 0; i < collection.size(); i++) {
        if (collection.get(i).getId().equals(id)) {
          if (executeScript) {
            movie = new ScanMovie(scanner, true).getMovie();
          }
          if (collection.update(id, movie, user)) {
            return new Response("Фильм c id " + id + " успешно обновлен", true);
          }
        }
      }
      return new Response("Фильм с таким ID не найден.", false);
    } catch (IllegalArgumentException e) {
      return new Response("Ошибка при обновлении фильма: " + e.getMessage(), false);
    }
  }
}
