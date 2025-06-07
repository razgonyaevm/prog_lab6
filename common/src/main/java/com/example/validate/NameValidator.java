package com.example.validate;

/** Валидация названия фильма и имени режиссера */
public class NameValidator implements Validator<String> {
  @Override
  public void validate(String value) {
    if (value == null || value.trim().isEmpty())
      throw new IllegalArgumentException("Movie name cannot be null or empty");
  }
}
