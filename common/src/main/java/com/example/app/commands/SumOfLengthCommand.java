package com.example.app.commands;

import com.example.app.Command;
import com.example.network.Response;
import com.example.service.MovieCollection;

/** Команда для вычисления суммы длин всех фильмов */
public class SumOfLengthCommand implements Command {
  private final MovieCollection collection;

  public SumOfLengthCommand(MovieCollection collection) {
    this.collection = collection;
  }

  @Override
  public Response execute() {
    return new Response(collection.sumOfLength(), true);
  }
}
