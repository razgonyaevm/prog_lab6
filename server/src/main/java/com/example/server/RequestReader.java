package com.example.server;

import com.example.app.CommandData;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* Модуль чтения запроса
 * Модуль десериализует входящие команды */
public class RequestReader {
  private static final Logger logger = LogManager.getLogger(RequestReader.class);

  public CommandData readCommandData(byte[] data) throws IOException {
    try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bis)) {
      return (CommandData) ois.readObject();
    } catch (ClassNotFoundException e) {
      logger.error("Ошибка десериализации команды", e);
      throw new IOException("Некорректная команда", e);
    }
  }
}
