package com.example.validate;

/** Валидация роста режиссера */
public class HeightValidator implements Validator<Long> {
  @Override
  public void validate(Long value) {
    if (value == null || value <= 0) {
      throw new IllegalArgumentException("Height must be greater than 0");
    }
  }
}
