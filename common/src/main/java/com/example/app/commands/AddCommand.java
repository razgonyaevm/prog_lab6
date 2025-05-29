package com.example.app.commands;

import com.example.app.Command;
import com.example.network.Response;
import com.example.service.MovieCollection;
import com.example.service.model.Movie;
import java.io.Serializable;
import lombok.Getter;

/** Команда для добавления нового фильма в коллекцию */
public class AddCommand implements Command, Serializable {
  private final MovieCollection collection; // Коллекция фильмов
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
  }

  /**
   * Выполняет команду добавления
   *
   * @return Ответ с результатом операции
   */
  @Override
  public Response execute() {
    try {
      if (movie == null) {
        return new Response("Ошибка: данные фильма не предоставлены", false);
      }
      movie.generateId();
      collection.add(movie);
      return new Response("Фильм успешно добавлен", true);
    } catch (Exception e) {
      return new Response("Ошибка при добавлении фильма: " + e.getMessage(), false);
    }
  }
}
