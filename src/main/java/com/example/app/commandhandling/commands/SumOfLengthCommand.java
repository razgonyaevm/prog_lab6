package com.example.app.commandhandling.commands;

import com.example.app.commandhandling.Command;
import com.example.service.MovieCollection;

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
