package com.example.common.app.commandhandling.commands;

import com.example.common.app.commandhandling.Command;
import com.example.common.network.Response;
import com.example.common.service.MovieCollection;

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
