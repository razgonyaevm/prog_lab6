package com.example.common.app.commandhandling.commands;

import com.example.common.app.commandhandling.Command;
import com.example.common.network.Response;

/** Завершает выполнение программы */
public class ExitCommand implements Command {
  @Override
  public Response execute() {
    return new Response("Программа завершена", true);
  }
}
