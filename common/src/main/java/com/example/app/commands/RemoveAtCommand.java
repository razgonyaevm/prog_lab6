package com.example.app.commands;

import static com.example.parsing.ParserClass.parseInt;

import com.example.app.Command;
import com.example.network.Response;
import com.example.service.MovieCollection;

/** Удаление элемента из коллекции по индексу */
public class RemoveAtCommand implements Command {
  private final MovieCollection collection;
  private final String command;

  public RemoveAtCommand(MovieCollection collection, String command) {
    this.collection = collection;
    this.command = command;
  }

  @Override
  public Response execute() {
    String[] parts = command.trim().split("\\s+");
    if (parts.length != 2) {
      return new Response("Ошибка: укажите id", false);
    }
    return collection.removeAt(parseInt(command.trim().split("\\s+")[1]));
  }
}
