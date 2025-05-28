package com.example.app.commands;

import com.example.app.Command;
import com.example.network.Response;
import com.example.service.MovieCollection;

/** Подсчет количества фильмов, у которых имя оператора равно заданному */
public class CountByOperator implements Command {
  private final MovieCollection collection;
  private final String operatorName;

  public CountByOperator(MovieCollection collection, String operatorName) {
    this.collection = collection;
    this.operatorName = operatorName;
  }

  @Override
  public Response execute() {
    if (operatorName == null || operatorName.isBlank()) {
      return new Response("Ошибка: укажите имя оператора", false);
    }
    return collection.countByOperator(operatorName);
  }
}
