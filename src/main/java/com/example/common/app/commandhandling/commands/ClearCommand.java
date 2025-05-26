package com.example.common.app.commandhandling.commands;

import com.example.common.app.commandhandling.Command;
import com.example.common.network.Response;
import com.example.common.service.MovieCollection;

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
