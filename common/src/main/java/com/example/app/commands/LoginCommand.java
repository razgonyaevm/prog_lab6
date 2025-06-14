package com.example.app.commands;

import com.example.app.Command;
import com.example.network.Response;
import com.example.service.UserManager;
import com.example.service.model.User;
import lombok.Getter;

@Getter
public class LoginCommand implements Command {
  private final String login;
  private final String password;

  public LoginCommand(String login, String password) {
    this.login = login;
    this.password = password;
  }

  @Override
  public Response execute(UserManager userManager) {
    User user = userManager.login(login, password);
    if (user != null) {
      return new Response("Авторизация успешна для пользователя " + login, true);
    }
    return new Response("Ошибка авторизации: неверные логин или пароль", false);
  }
}
