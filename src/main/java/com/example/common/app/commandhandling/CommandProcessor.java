package com.example.common.app.commandhandling;

import com.example.common.app.commandhandling.commands.*;
import com.example.common.network.Response;
import com.example.common.service.MovieCollection;
import java.util.Scanner;

public class CommandProcessor {
  public static Response processCommand(
      String command,
      MovieCollection collection,
      Scanner scanner,
      CommandInvoker invoker,
      Boolean executeScript) {
    String[] parts = command.trim().split("\\s+");
    try {
      switch (parts[0].toLowerCase()) {
        case "update" -> {
          if (executeScript) {
            return invoker.execute(new UpdateCommand(collection, scanner, command, true));
          }
          return new Response(
              "Команда 'update' должна содержать данные фильма и id фильма для обновления", false);
        }
        case "add" -> {
          if (executeScript) {
            return invoker.execute(new AddCommand(collection, scanner, true));
          }
          return new Response("Команда 'add' должна содержать данные фильма", false);
        }
        case "execute_script" -> {
          if (parts.length == 2) {
            return invoker.execute(new ExecuteScriptCommand(collection, parts[1], invoker));
          } else {
            return new Response("Ошибка: укажите имя файла", false);
          }
        }
        case "count_by_operator" -> {
          return invoker.execute(
              new CountByOperator(
                  collection, command.substring("count_by_operator".length()).trim()));
        }
        case "remove_by_id" -> {
          return invoker.execute(new RemoveByIdCommand(collection, command));
        }
        case "remove_at" -> {
          return invoker.execute(new RemoveAtCommand(collection, command));
        }
        default -> {
          return invoker.execute(command);
        }
      }
    } catch (Exception e) {
      return new Response("Ошибка выполнения программы: " + e.getMessage(), false);
    }
  }
}
