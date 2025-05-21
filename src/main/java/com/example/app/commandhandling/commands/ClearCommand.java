package com.example.app.commandhandling.commands;

import com.example.app.commandhandling.Command;
import com.example.service.MovieCollection;

/** Очищает коллекцию */
public class ClearCommand implements Command {
  private final MovieCollection collection;

  public ClearCommand(MovieCollection collection) {
    this.collection = collection;
  }

  @Override
  public void execute() {
    collection.clear();
    System.out.println("Коллекция очищена");
  }
}
