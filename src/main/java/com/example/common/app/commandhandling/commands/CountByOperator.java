package com.example.common.app.commandhandling.commands;

import com.example.common.app.commandhandling.Command;
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
  public void execute() {
    if (operatorName == null || operatorName.isBlank()) {
      System.out.println("Ошибка: укажите имя оператора");
      return;
    }
    collection.countByOperator(operatorName);
  }
}
