package com.example.app.commands;

import com.example.app.Command;
import com.example.network.Response;
import com.example.parsing.ScanMovie;
import com.example.service.MovieCollection;
import com.example.service.model.Movie;
import java.util.Scanner;

public class AddExecutionCommand implements Command {
  private final MovieCollection collection;
  private final Scanner scanner;

  public AddExecutionCommand(MovieCollection collection, Scanner scanner) {
    this.collection = collection;
    this.scanner = scanner;
  }

  @Override
  public Response execute() {
    Movie newMovie = new ScanMovie(scanner, true).getMovie();
    newMovie.generateId();
    return collection.add(newMovie);
  }
}
