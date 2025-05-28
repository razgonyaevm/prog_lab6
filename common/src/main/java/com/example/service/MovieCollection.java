package com.example.service;

import com.example.network.Response;
import com.example.service.model.Movie;
import com.example.xml.XMLHandler;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;

/** Класс для управления коллекцией с элементами {@link Movie} */
@Getter
public class MovieCollection implements Serializable {
  private final LinkedList<Movie> movies = new LinkedList<>();
  private final LocalDateTime initializationDate = LocalDateTime.now();

  /** Добавляет элемент в коллекцию */
  public Response add(Movie movie) {
    movies.add(movie);
    return new Response("Фильм успешно добавлен", true);
  }

  /** Устанавливает новый элемент на место с индексом id */
  public Response update(long id, Movie newMovie) {
    newMovie.updateId(id);

    ListIterator<Movie> iterator = movies.listIterator();
    while (iterator.hasNext()) {
      if (iterator.next().getId().equals(id)) {
        iterator.set(newMovie);
        return new Response("Фильм успешно обновлен", true);
      }
    }
    return new Response("Фильм с таким id не найден", false);
  }

  /** Удаляет элемент по значению его id */
  public Response removeById(long id) {
    boolean removed = movies.removeIf(movie -> movie.getId().equals(id));
    return removed
        ? new Response("Фильм с id " + id + " успешно удален", true)
        : new Response("Фильм с таким id не найден", false);
  }

  /** Очищает коллекцию */
  public Response clear() {
    movies.clear();
    return new Response("Коллекция очищена", true);
  }

  /** Выводит все элементы коллекции в строковом представлении */
  public Response show() {
    int index = 1; // Нумерация начинается с 1
    StringBuilder result = new StringBuilder();
    for (Movie movie : movies) {
      result.append("Фильм ").append(index).append(": ").append(movie).append("\n");
      index++;
    }
    if (movies.isEmpty()) {
      return new Response("В коллекции нет элементов", true);
    } else {
      return new Response(result.toString(), true);
    }
  }

  /** Меняет порядок элементов коллекции на противоположный */
  public Response reorder() {
    Collections.reverse(movies);
    return new Response("Элементы коллекции переставлены в обратном порядке", true);
  }

  /** Удаляет элемент коллекции по индексу */
  public Response removeAt(int index) {
    if (index >= 0 && index < movies.size()) {
      movies.remove(index);
      return new Response("Элемент удален", true);
    } else {
      return new Response("Ошибка: неверный индекс или элемент не найден", false);
    }
  }

  /** Удаляет первый элемент коллекции */
  public Response removeFirst() {
    if (!movies.isEmpty()) {
      movies.removeFirst();
      return new Response("Первый элемент коллекции удален", true);
    } else {
      return new Response("В коллекции нет элементов", false);
    }
  }

  /** Выводит сумму значений поля length для всех элементов коллекции */
  public Response sumOfLength() {
    int sum = movies.stream().mapToInt(Movie::getLength).sum();
    return new Response("Сумма length: " + sum, true);
  }

  /** Выводит количество элементов, у которых имя оператора равно заданному */
  public Response countByOperator(String operatorName) {
    long count =
        movies.stream()
            .filter(m -> Objects.equals(m.getOperator().getName(), operatorName))
            .count();
    return new Response("Количество фильмов оператора " + operatorName + ": " + count, true);
  }

  /** Выводит количество оскаров у всех фильмов в порядке убывания */
  public Response printDescendingOscarsCount() {
    String result =
        movies.stream()
            .map(Movie::getOscarsCount)
            .sorted(Comparator.reverseOrder())
            .map(String::valueOf)
            .collect(Collectors.joining(", "));
    return new Response(result.isEmpty() ? "Коллекция пуста" : result, true);
  }

  /** Устанавливает новую коллекцию */
  public Response setMovies(LinkedList<Movie> movies) {
    this.movies.clear();
    this.movies.addAll(movies);
    return new Response("Новая коллекция установлена", true);
  }

  /** Возвращает размер коллекции */
  public Response size() {
    return new Response(String.valueOf(movies.size()), true);
  }

  /** Сохраняет коллекцию в файл */
  public Response save(String filePath) {
    XMLHandler xmlHandler = new XMLHandler(filePath);
    return xmlHandler.save(movies);
  }

  /** Возвращает элемент коллекции по индексу */
  public Movie get(int index) {
    return movies.get(index);
  }
}
