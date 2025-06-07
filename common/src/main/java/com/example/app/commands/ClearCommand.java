package com.example.app.commands;

import com.example.app.Command;
import com.example.network.Response;
import com.example.service.MovieCollection;

/** Очищает коллекцию */
public class ClearCommand implements Command {
  private final MovieCollection collection;

  public ClearCommand(MovieCollection collection) {
    this.collection = collection;
  }

  @Override
  public Response execute() {
    collection.clear();
    return new Response("Коллекция очищена", true);
  }
}
