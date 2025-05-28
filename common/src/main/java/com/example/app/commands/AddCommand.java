package com.example.app.commands;

import com.example.app.Command;
import com.example.network.Response;
import com.example.parsing.ScanMovie;
import com.example.service.MovieCollection;
import com.example.service.model.Movie;

import java.io.Serializable;
import java.util.Scanner;
import lombok.Getter;

/** Команда для добавления нового фильма в коллекцию */
public class AddCommand implements Command, Serializable {
  private final MovieCollection collection; // Коллекция фильмов
  private final Scanner scanner;
  private final boolean executeScript;
  @Getter private final Movie movie; // Фильм для добавления

  /**
   * Конструктор команды добавления
   *
   * @param collection Коллекция, в которую добавляется фильм
   * @param movie Фильм для добавления
   */
  public AddCommand(MovieCollection collection, Movie movie) {
    this.collection = collection;
    this.movie = movie;
    this.scanner = null;
    this.executeScript = false;
  }

  public AddCommand(MovieCollection collection, Scanner scanner, Boolean executeScript) {
    this.collection = collection;
    this.scanner = scanner;
    this.movie = null;
    this.executeScript = executeScript;
  }

  /**
   * Выполняет команду добавления
   *
   * @return Ответ с результатом операции
   */
  @Override
  public Response execute() {
    try {
      if (executeScript) {
        Movie newMovie = new ScanMovie(scanner, true).getMovie();
        newMovie.generateId();
        return collection.add(newMovie);
      } else {
        if (movie == null) {
          return new Response("Ошибка: данные фильма не предоставлены", false);
        }
        movie.generateId();
        return collection.add(movie);
      }
    } catch (Exception e) {
      return new Response("Ошибка при добавлении фильма: " + e.getMessage(), false);
    }
  }
}
