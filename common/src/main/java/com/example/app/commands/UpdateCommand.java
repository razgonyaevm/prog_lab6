package com.example.app.commands;

import static com.example.parsing.ParserClass.parseLong;

import com.example.app.Command;
import com.example.network.Response;
import com.example.parsing.ScanMovie;
import com.example.service.MovieCollection;
import com.example.service.model.Movie;
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

  public UpdateCommand(MovieCollection collection, Movie movie, String command) {
    this.collection = collection;
    this.movie = movie;
    this.command = command;
    this.scanner = null;
    this.executeScript = false;
  }

  public UpdateCommand(
      MovieCollection collection, Scanner scanner, String command, Boolean executeScript) {
    this.collection = collection;
    this.scanner = scanner;
    this.executeScript = executeScript;
    this.command = command;
    this.movie = null;
  }

  @Override
  public Response execute() {
    String[] parts = command.trim().split("\\s+");
    if (parts.length != 2) {
      return new Response("Ошибка: укажите id", false);
    }
    try {
      long id = parseLong(parts[1]);
      if (id <= 0) {
        return new Response("ID must be greater than 0", false);
      }
      for (int i = 0; i < Integer.parseInt(collection.size().getMessage()); i++) {
        if (collection.get(i).getId().equals(id)) {
          if (executeScript) {
            movie = new ScanMovie(scanner, true).getMovie();
          }
          collection.update(id, movie);
        }
        return new Response("Фильм успешно обновлен", true);
      }
      return new Response("Фильм с таким ID не найден.", false);
    } catch (IllegalArgumentException e) {
      return new Response("Ошибка при обновлении фильма: " + e.getMessage(), false);
    }
  }
}
