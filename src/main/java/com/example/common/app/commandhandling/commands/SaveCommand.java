package com.example.common.app.commandhandling.commands;

import com.example.common.app.commandhandling.Command;
import com.example.common.network.Response;
import com.example.common.service.MovieCollection;

/** Команда сохранения коллекции в файл */
public class SaveCommand implements Command {
  private final MovieCollection collection;
  private final String filePath;

  public SaveCommand(MovieCollection collection, String filePath) {
    this.collection = collection;
    this.filePath = filePath;
  }

  @Override
  public Response execute() {
    return collection.save(filePath);
  }
}
