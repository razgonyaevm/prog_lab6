package com.example.app.commandhandling;

import java.util.HashMap;
import java.util.Map;

/** Класс, отвечающий за выполнение команд */
public class CommandInvoker {
  private final Map<String, Command> commands = new HashMap<>();

  public void register(String commandName, Command command) {
    commands.put(commandName, command);
  }

  public void execute(String commandName) {
    Command command = commands.get(commandName);
    if (command != null) {
      command.execute();
    } else {
      System.out.println("Ошибка в команде. Для вывода справки, воспользуйтесь командой help");
    }
  }

  public void execute(Command command) {
    command.execute();
  }
}
