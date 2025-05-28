package com.example.network;

import java.io.Serializable;
import lombok.Getter;

@Getter
public class Response implements Serializable {

  private final String message;
  private final boolean status;
  private final long timestamp;

  public Response(String message, boolean status) {
    this.message = message;
    this.status = status;
    this.timestamp = System.currentTimeMillis();
  }
}
