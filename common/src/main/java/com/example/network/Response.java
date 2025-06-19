package com.example.network;

import com.example.service.model.MovieCollectionDTO;
import java.io.Serializable;
import lombok.Getter;

/**
 * Класс используется при передаче ответа на команду от сервера message - сообщение, которое
 * передается клиенту status - статус выполнения команды (true - позитивный исход исполнения, false
 * - негативный). Клиент получает ответ и обрабатывает логгером согласно статусу response
 */
@Getter
public class Response implements Serializable {

  private final String message;
  private final boolean status;
  private final long timestamp;
  private final MovieCollectionDTO collection;

  public Response(String message, boolean status) {
    this.message = message;
    this.status = status;
    this.timestamp = System.currentTimeMillis();
    this.collection = null;
  }

  public Response(String message, boolean status, MovieCollectionDTO collection) {
    this.message = message;
    this.status = status;
    this.timestamp = System.currentTimeMillis();
    this.collection = collection;
  }
}
