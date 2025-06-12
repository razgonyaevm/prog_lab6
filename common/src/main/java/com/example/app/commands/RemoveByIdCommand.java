package com.example.app.commands;

import static com.example.parsing.ParserClass.parseLong;

import com.example.app.Command;
import com.example.network.Response;
import com.example.service.MovieCollection;
import com.example.service.UserManager;
import com.example.service.model.User;

/** Удаление элемента из коллекции по его id */
public class RemoveByIdCommand implements Command {
  private final MovieCollection collection;
  private final String command;
  private final String login;
  private final String password;

  public RemoveByIdCommand(
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
    long id = parseLong(parts[1]);
    return collection.removeById(id, user)
        ? new Response("Фильм с id " + id + " успешно удален", true)
        : new Response("Фильм с таким id не найден", false);
  }
}
