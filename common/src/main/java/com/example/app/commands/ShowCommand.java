package com.example.app.commands;

import com.example.app.Command;
import com.example.network.Response;
import com.example.service.MovieCollection;

/** Выводит все элементы коллекции в строковом представлении */
public class ShowCommand implements Command {
  private final MovieCollection collection;

  public ShowCommand(MovieCollection collection) {
    this.collection = collection;
  }

  @Override
  public Response execute() {
    String result = collection.show();
    return new Response(result, true);
  }
}
