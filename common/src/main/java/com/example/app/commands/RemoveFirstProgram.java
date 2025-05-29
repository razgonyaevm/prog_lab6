package com.example.app.commands;

import com.example.app.Command;
import com.example.network.Response;
import com.example.service.MovieCollection;

/** Удаление первого фильма из коллекции */
public class RemoveFirstProgram implements Command {
  private final MovieCollection collection;

  public RemoveFirstProgram(MovieCollection collection) {
    this.collection = collection;
  }

  @Override
  public Response execute() {
    return collection.removeFirst()
        ? new Response("Первый элемент коллекции удален", true)
        : new Response("В коллекции нет элементов", false);
  }
}
