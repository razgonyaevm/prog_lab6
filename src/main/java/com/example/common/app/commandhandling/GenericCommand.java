package com.example.common.app.commandhandling;

import com.example.common.network.Response;
import java.io.Serializable;

public class GenericCommand implements Command, Serializable {
  private final String commandString;

  public GenericCommand(String commandString) {
    this.commandString = commandString;
  }

  @Override
  public Response execute() {
    return new Response("Команда отправлена: " + commandString, true);
  }

  @Override
  public String toString() {
    return commandString;
  }
}
