package com.example.app.commands;

import com.example.app.Command;
import com.example.network.Response;
import com.example.service.UserManager;
import com.example.service.model.User;
import lombok.Getter;

@Getter
public class RegisterCommand implements Command {
  private final String login;
  private final String password;

  public RegisterCommand(String login, String password) {
    this.login = login;
    this.password = password;
  }

  @Override
  public Response execute(UserManager userManager) {
    User user = userManager.register(login, password);
    if (user != null) {
      return new Response("Регистрация успешна для пользователя " + login, true);
    }
    return new Response("Ошибка регистрации: логин уже существует", false);
  }
}
