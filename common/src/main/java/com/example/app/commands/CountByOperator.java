package com.example.app.commands;

import com.example.app.Command;
import com.example.network.Response;
import com.example.service.MovieCollection;
import com.example.service.UserManager;
import com.example.service.model.User;

/** Подсчет количества фильмов, у которых имя оператора равно заданному */
public class CountByOperator implements Command {
  private final MovieCollection collection;
  private final String input;
  private final String login;
  private final String password;

  public CountByOperator(MovieCollection collection, String input, String login, String password) {
    this.collection = collection;
    this.input = input;
    this.login = login;
    this.password = password;
  }

  @Override
  public Response execute(UserManager userManager) {
    User user = userManager.verify(login, password);
    if (user == null) {
      return new Response("Неавторизованный доступ", false);
    }
    String operatorName = input.trim().split("\\s+")[1];
    if (userManager.getCurrentUser() == null) {
      return new Response("Неавторизованный доступ", false);
    }
    if (operatorName == null || operatorName.isBlank()) {
      return new Response("Ошибка: укажите имя оператора", false);
    }
    return new Response(collection.countByOperator(operatorName), true);
  }
}
