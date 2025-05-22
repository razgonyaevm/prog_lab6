package com.example.common.app.commandhandling.commands;

import com.example.common.app.commandhandling.Command;
import com.example.common.service.MovieCollection;

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
