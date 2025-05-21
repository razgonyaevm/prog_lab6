package com.example.app.commandhandling.commands;

import com.example.app.commandhandling.Command;

/** Завершает выполнение программы */
public class ExitCommand implements Command {
  @Override
  public void execute() {
    System.out.println("Программа завершена");
    System.exit(0);
  }
}
