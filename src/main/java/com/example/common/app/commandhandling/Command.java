package com.example.common.app.commandhandling;

import com.example.common.network.Response;

/** Интерфейс для команд */
public interface Command {
  Response execute();
}
