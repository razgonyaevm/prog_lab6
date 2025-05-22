package com.example.common.app.commandhandling.commands;

import com.example.common.app.commandhandling.Command;
import com.example.common.service.model.Movie;
import com.example.common.service.MovieCollection;

/**
 * Выводит значения поля oscarsCount всех элементов {@link Movie}
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
