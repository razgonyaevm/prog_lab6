package com.example.app.commandhandling.commands;

import static com.example.app.commandhandling.CommandHandler.handleCommand;

import com.example.app.commandhandling.Command;
import com.example.app.commandhandling.CommandInvoker;
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
  public void execute() {
    Path script_path = Paths.get(fileName).toAbsolutePath().normalize();
    String normalizedFileName = script_path.toString();
    if (executingScripts.contains(normalizedFileName)) {
      System.out.println("Обнаружен рекурсивный вызов скрипта " + normalizedFileName);
      System.out.println("Текущая цепочка вызовов: " + executingScripts);
      return;
    }

    executingScripts.add(normalizedFileName);

    try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
      Scanner fileScanner = new Scanner(br);
      while (fileScanner.hasNextLine()) {
        String command = fileScanner.nextLine().trim();
        if (!command.isEmpty()) {
          handleCommand(command, collection, fileScanner, invoker, true);
        }
      }
      System.out.println("Выполнение скрипта " + fileName + " завершено");
    } catch (FileNotFoundException e) {
      System.out.println("Файл не найден");
    } catch (Exception e) {
      System.out.println("Ошибка выполнения скрипта: " + e.getMessage());
    } finally {
      executingScripts.pop();
    }
  }
}
