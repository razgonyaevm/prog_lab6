package com.example.app.commands;

import static com.example.app.CommandProcessor.processCommand;

import com.example.app.Command;
import com.example.app.CommandInvoker;
import com.example.network.Response;
import com.example.service.MovieCollection;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;

/** Выполнение скрипта */
public class ExecuteScriptCommand implements Command {
  private static final Deque<String> executingScripts = new ArrayDeque<>();

  private final MovieCollection collection;
  private final String fileName;
  private final CommandInvoker invoker;

  public ExecuteScriptCommand(MovieCollection collection, String fileName, CommandInvoker invoker) {
    this.collection = collection;
    this.fileName = fileName;
    this.invoker = invoker;
  }

  @Override
  public Response execute() {
    Path scriptPath = Paths.get(fileName).toAbsolutePath().normalize();
    String normalizedFileName = scriptPath.toString();
    if (executingScripts.contains(normalizedFileName)) {
      return new Response(
          "Обнаружен рекурсивный вызов скрипта "
              + normalizedFileName
              + '\n'
              + "Текущая цепочка вызовов: "
              + executingScripts,
          false);
    }

    executingScripts.add(normalizedFileName);

    try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
      Scanner fileScanner = new Scanner(br);
      StringBuilder responses = new StringBuilder();
      while (fileScanner.hasNextLine()) {
        String command = fileScanner.nextLine().trim();
        if (!command.isEmpty()) {
          responses.append(
              processCommand(command, collection, fileScanner, invoker, true).getMessage());
          responses.append("\n");
        }
      }
      return new Response(responses + "Выполнение скрипта " + fileName + " завершено", true);
    } catch (FileNotFoundException e) {
      return new Response("Файл не найден", false);
    } catch (Exception e) {
      return new Response("Ошибка выполнения скрипта: " + e.getMessage(), false);
    } finally {
      executingScripts.pop();
    }
  }
}
