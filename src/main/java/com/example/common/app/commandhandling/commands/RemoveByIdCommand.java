package com.example.common.app.commandhandling.commands;

import static com.example.common.parsing.ParserClass.parseLong;

import com.example.common.app.commandhandling.Command;
import com.example.common.service.MovieCollection;

/** Удаление элемента из коллекции по его id */
public class RemoveByIdCommand implements Command {
  private final MovieCollection collection;
  private final String command;

  public RemoveByIdCommand(MovieCollection collection, String command) {
    this.collection = collection;
    this.command = command;
  }

  @Override
  public void execute() {
    String[] parts = command.trim().split("\\s+");
    if (parts.length != 2) {
      System.out.println("Ошибка: укажите id");
      return;
    }
    try {
      collection.removeById(parseLong(parts[1]));
      System.out.println("Фильм успешно удален");
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }
}
