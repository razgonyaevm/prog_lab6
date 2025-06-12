package com.example.app.commands;

import static com.example.parsing.ParserClass.parseInt;

import com.example.app.Command;
import com.example.network.Response;
import com.example.service.MovieCollection;
import com.example.service.UserManager;
import com.example.service.model.User;

/** Удаление элемента из коллекции по индексу */
public class RemoveAtCommand implements Command {
  private final MovieCollection collection;
  private final String command;
  private final String login;
  private final String password;

  public RemoveAtCommand(
      MovieCollection collection, String command, String login, String password) {
    this.collection = collection;
    this.command = command;
    this.login = login;
    this.password = password;
  }

  @Override
  public Response execute(UserManager userManager) {
    User user = userManager.verify(login, password);
    if (user == null) {
      return new Response("Неавторизованный доступ", false);
    }
    String[] parts = command.trim().split("\\s+");
    if (parts.length != 2) {
      return new Response("Ошибка: укажите id", false);
    }
    return collection.removeAt(parseInt(command.trim().split("\\s+")[1]), user)
        ? new Response("Элемент удален", true)
        : new Response("Ошибка: неверный индекс, элемент не найден или доступ запрещен", false);
  }
}
