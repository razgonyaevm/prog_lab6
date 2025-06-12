package com.example.app;

import com.example.network.Response;
import com.example.service.UserManager;
import java.util.HashMap;
import java.util.Map;

/** Класс, отвечающий за выполнение команд */
public class CommandInvoker {
  private final Map<String, Command> commands = new HashMap<>();

  public void register(String commandName, Command command) {
    commands.put(commandName, command);
  }

  public Response execute(String commandName, UserManager userManager) {
    Command command = commands.get(commandName);
    if (command != null) {
      return command.execute(userManager);
    } else {
      return new Response(
          "Ошибка в команде. Для вывода справки, воспользуйтесь командой help", false);
    }
  }

  public Response execute(Command command, UserManager userManager) {
    return command.execute(userManager);
  }

  public boolean isRegistered(String commandName) {
    return commands.containsKey(commandName);
  }

  public Command getCommand(String commandName) {
    return commands.get(commandName);
  }
}
