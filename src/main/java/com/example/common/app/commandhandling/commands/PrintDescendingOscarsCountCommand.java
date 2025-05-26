package com.example.common.app.commandhandling.commands;

import com.example.common.app.commandhandling.Command;
import com.example.common.network.Response;
import com.example.common.service.MovieCollection;
import com.example.common.service.model.Movie;

/** Выводит значения поля oscarsCount всех элементов {@link Movie} коллекции в порядке убывания */
public class PrintDescendingOscarsCountCommand implements Command {
  private final MovieCollection collection;

  public PrintDescendingOscarsCountCommand(MovieCollection collection) {
    this.collection = collection;
  }

  @Override
  public Response execute() {
    return collection.printDescendingOscarsCount();
  }
}
