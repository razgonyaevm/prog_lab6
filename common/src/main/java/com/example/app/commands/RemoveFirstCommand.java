package com.example.app.commands;

import com.example.app.Command;
import com.example.network.Response;
import com.example.service.MovieCollection;
import com.example.service.UserManager;
import com.example.service.model.User;

/** Удаление первого фильма из коллекции */
public class RemoveFirstCommand implements Command {
  private final MovieCollection collection;
  private final String login;
  private final String password;

  public RemoveFirstCommand(MovieCollection collection, String login, String password) {
    this.collection = collection;
    this.login = login;
    this.password = password;
  }

  @Override
  public Response execute(UserManager userManager) {
    User user = userManager.verify(login, password);
    if (user == null) {
      return new Response("Неавторизованный доступ", false);
    }
    if (collection.removeFirst(user)) {
      return new Response("Первый элемент коллекции пользователя удален", true);
    }
    return new Response("Ошибка удаления первого элемента коллекции", false);
  }
}
