package com.example.common.app.commandhandling.commands;

import com.example.common.app.commandhandling.Command;
import com.example.common.service.MovieCollection;

/** Команда для вычисления суммы длин всех фильмов */
public class SumOfLengthCommand implements Command {
  private final MovieCollection collection;

  public SumOfLengthCommand(MovieCollection collection) {
    this.collection = collection;
  }

  @Override
  public void execute() {
    collection.sumOfLength();
  }
}
