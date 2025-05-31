package com.example.client;

import com.example.app.CommandData;
import com.example.network.Response;
import com.example.parsing.ScanMovie;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Client {
  private static final Logger logger = LogManager.getLogger(Client.class);
  private static final int TIMEOUT_MS = 5000;
  private static final int MAX_RETRIES = 3;
  private static final int SLEEP_MS = 1000;
  private static final int BUFFER_SIZE = 65535;

  public static void main(String[] args) {
    try (DatagramSocket socket = new DatagramSocket()) {
      socket.setSoTimeout(TIMEOUT_MS);
      InetAddress address = InetAddress.getByName(System.getenv("HOST"));
      int port = Integer.parseInt(System.getenv("PORT"));
      Scanner scanner = new Scanner(System.in);

      logger.info("Клиент запущен");

      while (true) {
        System.out.print("Введите команду: ");
        String input = scanner.nextLine();
        if (input.trim().equals("exit")) break;

        // Обрабатываем ввод пользователя
        List<CommandData> commands = processInput(input, scanner);
        for (CommandData command : commands) {
          sendCommand(socket, address, port, command);
        }
      }
    } catch (Exception e) {
      logger.error("Ошибка работы клиента: {}", e.getMessage(), e);
    }
  }

  private static List<CommandData> processInput(String input, Scanner scanner) {
    List<CommandData> commands = new ArrayList<>();
    String[] parts = input.trim().split("\\s+");
    String commandName = parts[0].toLowerCase();

    if (commandName.equals("execute_script") && parts.length == 2) {
      String filePath = parts[1];
      commands.addAll(executeScript(filePath, scanner));
    } else {
      commands.add(createCommandData(input, scanner, false));
    }
    return commands;
  }

  private static List<CommandData> executeScript(String filePath, Scanner scanner) {
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
              commands.add(createCommandData(line, fileScanner, true));
            } catch (IllegalArgumentException e) {
              logger.warn("Ошибка в команде: '{}': {}", line, e.getMessage());
              System.out.println("Ошибка в скрипте: " + e.getMessage());
            }
          }
        }
      } catch (FileNotFoundException e) {
        logger.error("Ошибка в чтения файла {}: {}", currentFile, e.getMessage());
        System.out.println("Ошибка чтения файла " + currentFile);
        return new ArrayList<>();
      }
    }
    return commands;
  }

  private static CommandData createCommandData(String input, Scanner scanner, boolean fromScript) {
    String[] parts = input.trim().split("\\s+");
    String commandName = parts[0].toLowerCase();

    return switch (commandName) {
      case "add" -> new CommandData("add", new ScanMovie(scanner, fromScript).getMovie());
      case "update" ->
          new CommandData(
              "update", new Object[] {new ScanMovie(scanner, fromScript).getMovie(), input});
      default -> new CommandData(commandName, input);
    };
  }

  private static void sendCommand(
      DatagramSocket socket, InetAddress address, int port, CommandData command) {
    int attempts = 0;
    boolean success = false;

    while (attempts < MAX_RETRIES && !success) {
      try {
        // Серилизируем программу
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(command);
        byte[] data = bos.toByteArray();

        // Отправляем датаграмму
        DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
        socket.send(packet);
        logger.debug("Отправлена команда: {}", command);

        // Получаем ответ
        byte[] buffer = new byte[BUFFER_SIZE];
        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(responsePacket);

        // Десерилизируем ответ
        ByteArrayInputStream bis = new ByteArrayInputStream(buffer, 0, responsePacket.getLength());
        ObjectInputStream ois = new ObjectInputStream(bis);
        Response response = (Response) ois.readObject();
        if (!response.isStatus()) {
          logger.error("Ответ сервера: {}", response.getMessage());
        } else {
          logger.info("Ответ сервера: {}", response.getMessage());
        }
        System.out.println("Ответ сервера: " + response.getMessage());

        success = true;
      } catch (IOException e) {
        attempts++;
        logger.warn("Попытка {} не удалась: {}", attempts, e.getMessage());
        if (attempts == MAX_RETRIES) {
          System.out.println("Сервер недоступен после " + MAX_RETRIES + " попыток");
        }
        try {
          Thread.sleep(SLEEP_MS);
        } catch (InterruptedException ie) {
          logger.error("Ошибка при ожидании: {}", ie.getMessage());
        }
      } catch (ClassNotFoundException e) {
        logger.error("Ошибка десериализации ответа: {}", e.getMessage());
        System.out.println("Ошибка десериализации ответа сервера");
        break;
      }
    }
  }
}
