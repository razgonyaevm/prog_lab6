package com.example.app.commands;

import com.example.app.Command;
import com.example.network.Response;
import com.example.service.MovieCollection;
import com.example.service.model.Movie;

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
