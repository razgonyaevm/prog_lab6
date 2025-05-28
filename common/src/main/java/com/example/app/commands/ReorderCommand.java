package com.example.app.commands;

import com.example.app.Command;
import com.example.network.Response;
import com.example.service.MovieCollection;

/** Изменяет порядок фильмов на обратный */
public class ReorderCommand implements Command {
  private final MovieCollection collection;

  public ReorderCommand(MovieCollection collection) {
    this.collection = collection;
  }

  @Override
  public Response execute() {
    return collection.reorder();
  }
}
