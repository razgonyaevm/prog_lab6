package com.example.client;

import com.example.app.CommandData;
import com.example.network.Response;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Client {
  private static final Logger logger = LogManager.getLogger(Client.class);
  private static final Dotenv dotenv = Dotenv.load();
  private static final int TIMEOUT_MS = 7000;
  private static final int MAX_RETRIES = 3;
  private static final int SLEEP_MS = 1000;
  private static final int BUFFER_SIZE = 65535;

  private final String host;
  private final int port;
  @Getter private String currentLogin;
  private String currentPassword;

  public Client() {
    this.host = dotenv.get("HOST");
    this.port = Integer.parseInt(dotenv.get("PORT"));
  }

  public boolean login(String username, String password) throws IOException {
    if (username == null || username.trim().isEmpty() || password == null) {
      throw new IllegalArgumentException("Логин и пароль не могут быть пустыми");
    }

    CommandData command = new CommandData("login", null, username, password);
    Response response = sendCommand(command);
    if (response != null && response.isStatus()) {
      currentLogin = username;
      currentPassword = password;
      logger.info("Успешный вход для пользователя {}", username);
      return true;
    } else {
      logger.warn(
          "Не удалось выполнить вход для пользователя {}. {}",
          username,
          response != null ? response.getMessage() : "Нет ответа от сервера");
      return false;
    }
  }

  public boolean register(String username, String password) throws IOException {
    if (username == null || username.trim().isEmpty() || password == null) {
      throw new IllegalArgumentException("Логин и пароль не могут быть пустыми");
    }

    CommandData command = new CommandData("register", null, username, password);
    Response response = sendCommand(command);
    if (response != null && response.isStatus()) {
      currentLogin = username;
      currentPassword = password;
      logger.info("Успешная регистрация пользователя {}", username);
      return true;
    } else {
      logger.warn(
          "Не удалось выполнить регистрацию пользователя {}. {}",
          username,
          response != null ? response.getMessage() : "Нет ответа от сервера");
      return false;
    }
  }

  public String sendCommand(String commandName, Object arguments) throws IOException {
    CommandData command = new CommandData(commandName, arguments, currentLogin, currentPassword);
    Response response = sendCommand(command);
    if (response == null) {
      throw new IOException("Сервер недоступен после " + MAX_RETRIES + " попыток");
    }
    return response.getMessage();
  }

  public Response getCollectionResponse() throws IOException {
    CommandData command = new CommandData("get_collection", null, currentLogin, currentPassword);
    Response response = sendCommand(command);
    if (response == null) {
      throw new IOException("Сервер недоступен после " + MAX_RETRIES + " попыток");
    }
    return response;
  }

  private Response sendCommand(CommandData command) throws IOException {
    int attempts = 0;
    boolean success = false;

    try (DatagramSocket socket = new DatagramSocket()) {
      socket.setSoTimeout(TIMEOUT_MS);
      InetAddress address = InetAddress.getByName(host);

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
          ByteArrayInputStream bis =
              new ByteArrayInputStream(buffer, 0, responsePacket.getLength());
          ObjectInputStream ois = new ObjectInputStream(bis);
          Response response = (Response) ois.readObject();
          if (!response.isStatus()) {
            logger.error("Ответ сервера: {}", response.getMessage());
          } else {
            logger.info("Ответ сервера: {}", response.getMessage());
          }
          success = true;
          return response;
        } catch (IOException e) {
          attempts++;
          logger.warn("Попытка {} не удалась: {}", attempts, e.getMessage());
          if (attempts == MAX_RETRIES) {
            throw new IOException("Сервер недоступен после " + MAX_RETRIES + " попыток");
          }
          try {
            Thread.sleep(SLEEP_MS);
          } catch (InterruptedException ie) {
            logger.error("Ошибка при ожидании: {}", ie.getMessage());
            Thread.currentThread().interrupt();
          }
        } catch (ClassNotFoundException e) {
          logger.error("Ошибка десериализации ответа: {}", e.getMessage());
          throw new IOException("Ошибка десериализации ответа сервера", e);
        }
      }
      return null;
    }
  }
}
