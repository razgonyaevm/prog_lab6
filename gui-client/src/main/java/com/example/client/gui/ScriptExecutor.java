package com.example.client.gui;

import com.example.app.CommandData;
import com.example.client.Client;
import com.example.service.enums.Color;
import com.example.service.enums.Country;
import com.example.service.enums.MovieGenre;
import com.example.service.enums.MpaaRating;
import com.example.service.model.Coordinates;
import com.example.service.model.Movie;
import com.example.service.model.Operator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ScriptExecutor {
  private final Client client;
  private final Set<String> visitedFiles;
  private final Stack<String> scriptStack;
  private final List<String> results;

  public ScriptExecutor(Client client) {
    this.client = client;
    this.visitedFiles = new HashSet<>();
    this.scriptStack = new Stack<>();
    this.results = new ArrayList<>();
  }

  public List<String> executeScript(String filePath) {
    results.clear();
    visitedFiles.clear();
    scriptStack.clear();
    scriptStack.push(filePath);
    processScripts();
    return new ArrayList<>(results);
  }

  private void processScripts() {
    while (!scriptStack.isEmpty()) {
      String currentFile = scriptStack.pop();
      if (!visitedFiles.add(currentFile)) {
        results.add(LocalizationManager.getString("script.error.recursion") + ": " + currentFile);
        return;
      }

      File file = new File(currentFile);
      if (!file.exists()) {
        results.add(
            LocalizationManager.getString("script.error.file_not_found") + ": " + currentFile);
        return;
      }

      try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
          lines.add(line.trim());
        }
        executeLines(lines);
      } catch (IOException e) {
        results.add(LocalizationManager.getString("script.error.reading") + ": " + e.getMessage());
      }
    }
  }

  private void executeLines(List<String> lines) {
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i).trim();
      if (line.isEmpty()) continue;

      String[] parts = line.split("\\s+");
      String commandName = parts[0].toLowerCase();

      if (commandName.equals("execute_script") && parts.length == 2) {
        String nestedFilePath = parts[1];
        scriptStack.push(nestedFilePath);
        return;
      }

      try {
        CommandData command = createCommandData(line, lines, i);
        String response = client.sendCommand(command.name(), command.arguments());
        results.add("Команда '" + line + "': " + response + "\n");
        if (commandName.equals("add") || commandName.equals("update")) {
          i += 12; // не обрабатываем строки, которые относятся к Movie
        }
      } catch (Exception e) {
        results.add(
            LocalizationManager.getString("script.error.invalid_command")
                + ": "
                + line
                + " ("
                + e.getMessage()
                + ")");
      }
    }
  }

  private CommandData createCommandData(String line, List<String> lines, int currentIndex) {
    String[] parts = line.trim().split("\\s+");
    String commandName = parts[0].toLowerCase();

    return switch (commandName) {
      case "add" -> {
        Movie movie = parseMovie(lines, currentIndex + 1);
        yield new CommandData("add", movie, client.getCurrentLogin(), null);
      }
      case "update" -> {
        Movie movie = parseMovie(lines, currentIndex + 1);
        yield new CommandData("update", new Object[] {movie, line}, client.getCurrentLogin(), null);
      }
      default -> new CommandData(commandName, line, client.getCurrentLogin(), null);
    };
  }

  private Movie parseMovie(List<String> lines, int startIndex) {
    try {
      int index = startIndex;
      String name = lines.get(index++).trim();
      double x = Double.parseDouble(lines.get(index++).trim());
      long y = Long.parseLong(lines.get(index++).trim());
      int oscarsCount = Integer.parseInt(lines.get(index++).trim());
      int length = Integer.parseInt(lines.get(index++).trim());
      MovieGenre genre = MovieGenre.valueOf(lines.get(index++).trim().toUpperCase());
      MpaaRating mpaaRating = MpaaRating.valueOf(lines.get(index++).trim().toUpperCase());
      String operatorName = lines.get(index++).trim();
      long height = Long.parseLong(lines.get(index++).trim());
      float weight = Float.parseFloat(lines.get(index++).trim());
      Color eyeColor = Color.valueOf(lines.get(index++).trim().toUpperCase());
      Country nationality = Country.valueOf(lines.get(index++).trim().toUpperCase());

      Coordinates coordinates = new Coordinates(x, y);
      Operator operator = new Operator(operatorName, height, weight, eyeColor, nationality);
      Movie movie = new Movie();
      movie.setName(name);
      movie.setCoordinates(coordinates);
      movie.setOscarsCount(oscarsCount);
      movie.setLength(length);
      movie.setGenre(genre);
      movie.setMpaaRating(mpaaRating);
      movie.setOperator(operator);
      movie.setOwner(new com.example.service.model.User(0, client.getCurrentLogin()));

      return movie;
    } catch (Exception e) {
      throw new IllegalArgumentException("Некорректные данные для фильма в скрипте");
    }
  }
}
