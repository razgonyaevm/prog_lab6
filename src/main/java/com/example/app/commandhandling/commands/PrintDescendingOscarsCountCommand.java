package com.example.app.commandhandling.commands;

import com.example.app.commandhandling.Command;
import com.example.service.MovieCollection;

/**
 * Выводит значения поля oscarsCount всех элементов {@link com.example.service.model.Movie}
 * коллекции в порядке убывания
 */
public class PrintDescendingOscarsCountCommand implements Command {
  private final MovieCollection collection;

  public PrintDescendingOscarsCountCommand(MovieCollection collection) {
    this.collection = collection;
  }

  @Override
  public void execute() {
    collection.printDescendingOscarsCount();
  }
}
