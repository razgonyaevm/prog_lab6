package com.example.service;

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
  private final List<Movie> movies = new LinkedList<>();
  private final LocalDateTime initializationDate = LocalDateTime.now();

  /** Добавляет элемент в коллекцию */
  public void add(Movie movie) {
    movies.add(movie);
  }

  /** Устанавливает новый элемент на место с индексом id */
  public void update(long id, Movie newMovie) {
    newMovie.updateId(id);

    ListIterator<Movie> iterator = movies.listIterator();
    while (iterator.hasNext()) {
      if (iterator.next().getId().equals(id)) {
        iterator.set(newMovie);
        return;
      }
    }
  }

  /** Удаляет элемент по значению его id */
  public boolean removeById(long id) {
    return movies.removeIf(movie -> movie.getId().equals(id));
  }

  /** Очищает коллекцию */
  public void clear() {
    movies.clear();
  }

  /** Выводит все элементы коллекции в строковом представлении */
  public String show() {
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
  }

  /** Меняет порядок элементов коллекции на противоположный */
  public void reorder() {
    Collections.reverse(movies);
  }

  /** Удаляет элемент коллекции по индексу */
  public boolean removeAt(int index) {
    if (index >= 0 && index < movies.size()) {
      movies.remove(index);
      return true;
    } else {
      return false;
    }
  }

  /** Удаляет первый элемент коллекции */
  public boolean removeFirst() {
    if (!movies.isEmpty()) {
      ((LinkedList<Movie>) movies).removeFirst();  // Без явного приведения не компилируется
      return true;
    } else {
      return false;
    }
  }

  /** Выводит сумму значений поля length для всех элементов коллекции */
  public String sumOfLength() {
    int sum = movies.stream().mapToInt(Movie::getLength).sum();
    return "Сумма length: " + sum;
  }

  /** Выводит количество элементов, у которых имя оператора равно заданному */
  public String countByOperator(String operatorName) {
    long count =
        movies.stream()
            .filter(m -> Objects.equals(m.getOperator().getName(), operatorName))
            .count();
    return "Количество фильмов оператора " + operatorName + ": " + count;
  }

  /** Выводит количество оскаров у всех фильмов в порядке убывания */
  public String printDescendingOscarsCount() {
    String result =
        movies.stream()
            .map(Movie::getOscarsCount)
            .sorted(Comparator.reverseOrder())
            .map(String::valueOf)
            .collect(Collectors.joining(", "));
    return result.isEmpty() ? "Коллекция пуста" : result;
  }

  /** Устанавливает новую коллекцию */
  public void setMovies(LinkedList<Movie> movies) {
    this.movies.clear();
    this.movies.addAll(movies);
  }

  /** Возвращает размер коллекции */
  public int size() {
    return movies.size();
  }

  /** Сохраняет коллекцию в файл */
  public String save(String filePath) {
    XMLHandler xmlHandler = new XMLHandler(filePath);
    return xmlHandler.save((LinkedList<Movie>) movies);
  }

  /** Возвращает элемент коллекции по индексу */
  public Movie get(int index) {
    return movies.get(index);
  }
}
