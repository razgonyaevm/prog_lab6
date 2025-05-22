package com.example.common.app.commandhandling.commands;

import com.example.common.app.commandhandling.Command;
import com.example.common.service.MovieCollection;

/** Вывод информации о коллекции */
public class InfoCommand implements Command {
  private final MovieCollection collection;

  public InfoCommand(MovieCollection collection) {
    this.collection = collection;
  }

  @Override
  public void execute() {
    System.out.println("Тип коллекции: " + collection.getMovies().getClass().getSimpleName());
    System.out.println("Количество элементов: " + collection.size());
    System.out.println("Дата инициализации: " + collection.getInitializationDate());
  }
}
