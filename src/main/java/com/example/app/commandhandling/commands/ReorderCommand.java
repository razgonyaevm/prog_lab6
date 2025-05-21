package com.example.app.commandhandling.commands;

import com.example.app.commandhandling.Command;
import com.example.service.MovieCollection;

/** Изменяет порядок фильмов на обратный */
public class ReorderCommand implements Command {
  private final MovieCollection collection;

  public ReorderCommand(MovieCollection collection) {
    this.collection = collection;
  }

  @Override
  public void execute() {
    collection.reorder();
    System.out.println("Порядок фильмов успешно изменен");
  }
}
