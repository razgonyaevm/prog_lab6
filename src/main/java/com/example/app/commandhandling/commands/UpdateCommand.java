package com.example.app.commandhandling.commands;

import static com.example.parsing.ParserClass.parseLong;

import com.example.app.commandhandling.Command;
import com.example.parsing.ScanMovie;
import com.example.service.MovieCollection;
import com.example.service.model.Movie;
import java.util.Scanner;

/** Обновление элемента в коллекции по его id */
public class UpdateCommand implements Command {
  private final MovieCollection collection;
  private final Scanner scanner;
  private final String command;
  private final Boolean execute_script;

  public UpdateCommand(
      MovieCollection collection, Scanner scanner, String command, Boolean execute_script) {
    this.collection = collection;
    this.scanner = scanner;
    this.command = command;
    this.execute_script = execute_script;
  }

  @Override
  public void execute() {
    String[] parts = command.trim().split("\\s+");
    if (parts.length != 2) {
      System.out.println("Ошибка: укажите id");
      return;
    }
    try {
      long id = parseLong(parts[1]);
      if (id <= 0) {
        System.out.println("ID must be greater than 0");
        return;
      }
      for (int i = 0; i < collection.size(); i++) {
        if (collection.get(i).getId().equals(id)) {
          Movie movieUpdate = new ScanMovie(scanner, execute_script).getMovie();
          collection.update(id, movieUpdate);
          System.out.println("Фильм успешно обновлен");
          return;
        }
      }
      System.out.println("Фильм с таким ID не найден.");
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }
}
