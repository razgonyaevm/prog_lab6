package com.example.app;

import com.example.network.Response;
import com.example.service.UserManager;

/** Интерфейс для команд */
public interface Command {
  Response execute(UserManager userManager);
}
