package com.example.validate;

/** Валидация длительности фильма */
public class LengthValidator implements Validator<Integer> {
  @Override
  public void validate(Integer value) {
    if (value == null || value <= 0) {
      throw new IllegalArgumentException("Length must be a positive number");
    }
  }
}
