package com.example.app.commandhandling;

import com.example.app.commandhandling.commands.*;
import com.example.service.MovieCollection;
import java.util.Scanner;

/** Обработка команд */
public class CommandHandler {
  public static void handleCommand(
      String command,
      MovieCollection collection,
      Scanner scanner,
      CommandInvoker invoker,
      Boolean execute_script) {
    String[] parts = command.trim().split("\\s+");

    switch (parts[0]) {
      case "update" ->
          invoker.execute(new UpdateCommand(collection, scanner, command, execute_script));
      case "add" -> invoker.execute(new AddCommand(collection, scanner, execute_script));
      case "execute_script" -> {
        if (parts.length == 2) {
          invoker.execute(new ExecuteScriptCommand(collection, parts[1], invoker));
        } else {
          System.out.println("Ошибка: укажите имя файла");
        }
      }
      case "save" -> {
        if (parts.length == 2) {
          invoker.execute(new SaveCommand(collection, parts[1]));
        } else {
          invoker.execute(command);
        }
      }
      case "count_by_operator" ->
          invoker.execute(
              new CountByOperator(
                  collection, command.substring("count_by_operator".length()).trim()));

      case "remove_by_id" -> invoker.execute(new RemoveByIdCommand(collection, command));
      case "remove_at" -> invoker.execute(new RemoveAtCommand(collection, command));
      default -> invoker.execute(command);
    }
  }
}
