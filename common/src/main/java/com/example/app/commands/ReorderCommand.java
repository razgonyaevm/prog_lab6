package com.example.app.commands;

import com.example.app.Command;
import com.example.network.Response;
import com.example.service.MovieCollection;
import com.example.service.UserManager;
import com.example.service.model.User;

/** Изменяет порядок фильмов на обратный */
public class ReorderCommand implements Command {
  private final MovieCollection collection;
  private final String login;
  private final String password;

  public ReorderCommand(MovieCollection collection, String login, String password) {
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
    collection.reorder();
    return new Response("Элементы коллекции переставлены в обратном порядке", true);
  }
}
