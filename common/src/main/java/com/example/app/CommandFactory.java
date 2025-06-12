package com.example.app;

import com.example.service.MovieCollection;

public interface CommandFactory {
  Command createCommand(
      MovieCollection collection, CommandData commandData, String login, String password);

  default boolean requiresAuth() {
    return true;
  }
}
