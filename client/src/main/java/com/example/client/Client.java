package com.example.client;

import com.example.app.Command;
import com.example.app.GenericCommand;
import com.example.app.commands.*;
import com.example.network.Response;
import com.example.parsing.ScanMovie;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Client {
  private static final Logger logger = LogManager.getLogger(Client.class);
  private static final int TIMEOUT_MS = 5000;
  private static final int MAX_RETRIES = 3;

  public static void main(String[] args) {
    try (DatagramSocket socket = new DatagramSocket()) {
      socket.setSoTimeout(TIMEOUT_MS);
      InetAddress address = InetAddress.getByName("localhost");
      int port = 12345;
      Scanner scanner = new Scanner(System.in);

      logger.info("Клиент запущен");

      while (true) {
        System.out.print("Введите команду: ");
        String input = scanner.nextLine();
        if (input.trim().equals("exit")) break;

        Command command;
        switch (input.trim().split("\\s+")[0]) {
          case "add" -> command = new AddCommand(null, new ScanMovie(scanner, false).getMovie());
          case "update" ->
              command = new UpdateCommand(null, new ScanMovie(scanner, false).getMovie(), input);
          default -> command = new GenericCommand(input);
        }

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

            // Получаем ответ
            byte[] buffer = new byte[65535];
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(responsePacket);

            // Десерилизируем ответ
            ByteArrayInputStream bis =
                new ByteArrayInputStream(buffer, 0, responsePacket.getLength());
            ObjectInputStream ois = new ObjectInputStream(bis);
            Response response = (Response) ois.readObject();
            System.out.println("Ответ сервера: " + response.getMessage());
            success = true;
          } catch (IOException e) {
            attempts++;
            logger.warn("Попытка {} не удалась: {}", attempts, e.getMessage());
            if (attempts == MAX_RETRIES) {
              System.out.println("Сервер недоступен после " + MAX_RETRIES + " попыток");
            }
            Thread.sleep(100);
          }
        }
      }
    } catch (Exception e) {
      logger.error("Ошибка работы клиента", e);
    }
  }
}
