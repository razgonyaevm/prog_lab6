package com.example.app;

import com.example.network.Response;

/** Интерфейс для команд */
public interface Command {
  Response execute();
}
