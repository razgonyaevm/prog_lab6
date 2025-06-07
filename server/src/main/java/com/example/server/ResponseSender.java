package com.example.server;

import com.example.network.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResponseSender {
  private static final Logger logger = LogManager.getLogger(ResponseSender.class);

  public void sendResponse(DatagramChannel channel, SocketAddress address, Response response)
      throws IOException {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeObject(response);
      byte[] data = bos.toByteArray();
      channel.send(ByteBuffer.wrap(data), address);
      logger.info("Ответ направлен клиенту {}", address);
    } catch (IOException e) {
      logger.error("Ошибка отправки ответа", e);
      throw e;
    }
  }
}
