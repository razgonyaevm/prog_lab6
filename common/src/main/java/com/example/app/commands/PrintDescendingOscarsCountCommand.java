package com.example.app.commands;

import com.example.app.Command;
import com.example.network.Response;
import com.example.service.MovieCollection;
import com.example.service.UserManager;
import com.example.service.model.Movie;
import com.example.service.model.User;

/** Выводит значения поля oscarsCount всех элементов {@link Movie} коллекции в порядке убывания */
public class PrintDescendingOscarsCountCommand implements Command {
  private final MovieCollection collection;
  private final String login;
  private final String password;

  public PrintDescendingOscarsCountCommand(
      MovieCollection collection, String login, String password) {
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
    return new Response(collection.printDescendingOscarsCount(), true);
  }
}
