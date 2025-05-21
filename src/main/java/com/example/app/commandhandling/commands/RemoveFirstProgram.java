package com.example.app.commandhandling.commands;

import com.example.app.commandhandling.Command;
import com.example.service.MovieCollection;

/** Удаление первого фильма из коллекции */
public class RemoveFirstProgram implements Command {
  private final MovieCollection collection;

  public RemoveFirstProgram(MovieCollection collection) {
    this.collection = collection;
  }

  @Override
  public void execute() {
    collection.removeFirst();
    System.out.println("Первый фильм успешно удален");
  }
}
