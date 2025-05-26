package com.example.common.app.commandhandling.commands;

import com.example.common.app.commandhandling.Command;
import com.example.common.network.Response;
import com.example.common.service.MovieCollection;

/** Выводит все элементы коллекции в строковом представлении */
public class ShowCommand implements Command {
  private final MovieCollection collection;

  public ShowCommand(MovieCollection collection) {
    this.collection = collection;
  }

  @Override
  public Response execute() {
    return collection.show();
  }
}
