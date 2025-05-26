package com.example.common.app.commandhandling.commands;

import com.example.common.app.commandhandling.Command;
import com.example.common.network.Response;
import com.example.common.service.MovieCollection;

/** Вывод информации о коллекции */
public class InfoCommand implements Command {
  private final MovieCollection collection;

  public InfoCommand(MovieCollection collection) {
    this.collection = collection;
  }

  @Override
  public Response execute() {
    return new Response(
        "Тип коллекции: "
            + collection.getMovies().getClass().getSimpleName()
            + '\n'
            + "Количество элементов: "
            + collection.getMovies().toArray().length
            + '\n'
            + "Дата инициализации: "
            + collection.getInitializationDate(),
        true);
  }
}
