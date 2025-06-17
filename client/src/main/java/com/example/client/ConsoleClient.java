package com.example.client;

import com.example.app.CommandData;
import com.example.parsing.ScanMovie;
import java.io.*;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConsoleClient {
  private static final Logger logger = LogManager.getLogger(ConsoleClient.class);
  private static boolean EXIT_FLAG = false;

  public static void main(String[] args) {
    Client client = new Client();
    Scanner scanner = new Scanner(System.in);

    logger.info("Клиент запущен");

    // Запрос авторизации или регистрации
    while (client.getCurrentLogin() == null && !EXIT_FLAG) {
      System.out.println("Введите 'register' для регистрации или 'login' для входа:");
      String authCommand = scanner.nextLine().trim().toLowerCase();
      if (authCommand.equals("exit")) {
        EXIT_FLAG = true;
        break;
      }
      if (!authCommand.equals("register") && !authCommand.equals("login")) {
        System.out.println("Неверная команда. Используйте 'register' или 'login'.");
        continue;
      }

      System.out.print("Логин: ");
      String login = scanner.nextLine().trim();
      System.out.print("Пароль: ");
      String password = scanner.nextLine().trim();

      try {
        boolean success =
            authCommand.equals("login")
                ? client.login(login, password)
                : client.register(login, password);
        if (!success) {
          System.out.println("Ошибка авторизации. Попробуйте снова.");
        }
      } catch (IOException e) {
        logger.error("Ошибка авторизации: {}", e.getMessage());
        System.out.println("Ошибка связи с сервером: " + e.getMessage());
      }
    }

    while (!EXIT_FLAG) {
      System.out.print("Введите команду: ");
      String input = scanner.nextLine();
      if (input.trim().equals("exit")) break;

      // Обрабатываем ввод пользователя
      List<CommandData> commands = processInput(input, scanner, client.getCurrentLogin());
      for (CommandData command : commands) {
        try {
          String response = client.sendCommand(command.name(), command.arguments());
          System.out.println("Ответ сервера: " + response);
        } catch (IOException e) {
          logger.error("Ошибка отправки команды: {}", e.getMessage());
          System.out.println("Не удалось получить ответ от сервера: " + e.getMessage());
        }
      }
    }
  }

  private static List<CommandData> processInput(
      String input, Scanner scanner, String currentLogin) {
    List<CommandData> commands = new ArrayList<>();
    String[] parts = input.trim().split("\\s+");
    String commandName = parts[0].toLowerCase();

    if (commandName.equals("execute_script") && parts.length == 2) {
      String filePath = parts[1];
      commands.addAll(executeScript(filePath, scanner, currentLogin));
    } else {
      commands.add(createCommandData(input, scanner, false, currentLogin));
    }
    return commands;
  }

  private static List<CommandData> executeScript(
      String filePath, Scanner scanner, String currentLogin) {
    List<CommandData> commands = new ArrayList<>();
    Stack<String> scriptStack = new Stack<>();
    Set<String> visitedFiles = new HashSet<>();
    scriptStack.push(filePath);

    while (!scriptStack.isEmpty()) {
      String currentFile = scriptStack.pop();
      if (!visitedFiles.add(currentFile)) {
        logger.warn("Обнаружена рекурсия: файл {} уже вызывался", currentFile);
        System.out.println("Ошибка: рекурсивный вызов скрипта " + currentFile);
        return new ArrayList<>();
      }
      File file = new File(currentFile);

      if (!file.exists()) {
        logger.warn("Файл скрипта {} не найден", currentFile);
        System.out.println("Ошибка: файл " + currentFile + " не найден");
        return new ArrayList<>();
      }

      try (Scanner fileScanner = new Scanner(file)) {
        while (fileScanner.hasNextLine()) {
          String line = fileScanner.nextLine().trim();
          if (line.isEmpty()) continue;

          String[] parts = line.split("\\s+");
          String commandName = parts[0].toLowerCase();

          if (commandName.equals("execute_script") && parts.length == 2) {
            String nestedFilePath = parts[1];
            scriptStack.push(nestedFilePath);
          } else {
            try {
              commands.add(createCommandData(line, fileScanner, true, currentLogin));
            } catch (IllegalArgumentException e) {
              logger.warn("Ошибка в команде: '{}': {}", line, e.getMessage());
              System.out.println("Ошибка в скрипте: " + e.getMessage());
            }
          }
        }
      } catch (FileNotFoundException e) {
        logger.error("Ошибка чтения файла {}: {}", currentFile, e.getMessage());
        System.out.println("Ошибка чтения файла " + currentFile);
        return new ArrayList<>();
      }
    }
    return commands;
  }

  private static CommandData createCommandData(
      String input, Scanner scanner, boolean fromScript, String currentLogin) {
    String[] parts = input.trim().split("\\s+");
    String commandName = parts[0].toLowerCase();

    return switch (commandName) {
      case "add" ->
          new CommandData("add", new ScanMovie(scanner, fromScript).getMovie(), currentLogin, null);
      case "update" ->
          new CommandData(
              "update",
              new Object[] {new ScanMovie(scanner, fromScript).getMovie(), input},
              currentLogin,
              null);
      default -> new CommandData(commandName, input, currentLogin, null);
    };
  }
}
