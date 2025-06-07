package com.example.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Модуль приёма подключений Класс для работы с сетевым каналом */
public class ConnectionHandler {
  private static final Logger logger = LogManager.getLogger(ConnectionHandler.class);
  private static final int BUFFER_SIZE = 65535;

  @Getter private final DatagramChannel channel;
  private final ByteBuffer buffer;

  public ConnectionHandler(String ip, int port) throws IOException {
    this.channel = DatagramChannel.open();
    this.channel.configureBlocking(false);
    this.channel.socket().bind(new InetSocketAddress(ip, port));
    this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
    logger.info("Сервер запущен на порту {}", port);
  }

  public ReceiveResult receive() throws IOException {
    buffer.clear();
    var clientAddress = channel.receive(buffer);
    if (clientAddress != null) {
      logger.info("Получен запрос от {}", clientAddress);
      buffer.flip();
      return new ReceiveResult(buffer, clientAddress);
    }
    return null;
  }
}
