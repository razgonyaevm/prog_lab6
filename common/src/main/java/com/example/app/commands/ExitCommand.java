package com.example.app.commands;

import com.example.app.Command;
import com.example.network.Response;

/** Завершает выполнение программы */
public class ExitCommand implements Command {
  @Override
  public Response execute() {
    return new Response("Программа завершена", true);
  }
}
