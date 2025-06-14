package com.example.service.model;

import java.io.Serializable;
import lombok.Getter;

@Getter
public class User implements Serializable {
  private final int id;
  private final String login;

  public User(int id, String login) {
    this.id = id;
    this.login = login;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    User user = (User) o;
    return id == user.id && login.equals(user.login);
  }

  @Override
  public int hashCode() {
    return 31 * id + login.hashCode();
  }

  @Override
  public String toString() {
    return login;
  }
}
