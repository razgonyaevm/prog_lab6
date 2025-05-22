package com.example.common.app.commandhandling.commands;

import com.example.common.app.commandhandling.Command;

/** Завершает выполнение программы */
public class ExitCommand implements Command {
  @Override
  public void execute() {
    System.out.println("Программа завершена");
    System.exit(0);
  }
}
