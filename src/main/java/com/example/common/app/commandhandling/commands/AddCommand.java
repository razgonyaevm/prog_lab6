package com.example.common.app.commandhandling.commands;

import com.example.common.app.commandhandling.Command;
import com.example.common.parsing.ScanMovie;
import com.example.common.service.MovieCollection;
import com.example.common.service.model.Movie;
import java.util.Scanner;

/** Добавление элемента в коллекцию */
public class AddCommand implements Command {
  private final MovieCollection collection;
  private final Scanner scanner;
  private final Boolean execute_script;

  public AddCommand(MovieCollection collection, Scanner scanner, Boolean execute_script) {
    this.collection = collection;
    this.scanner = scanner;
    this.execute_script = execute_script;
  }

  @Override
  public void execute() {
    try {
      Movie movie = new ScanMovie(scanner, execute_script).getMovie();
      movie.generateId();
      collection.add(movie);
      System.out.println("Фильм успешно добавлен");
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }
}
