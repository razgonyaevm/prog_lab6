package com.example.validate;

/** Валидация количества оскаров фильма */
public class OscarsCountValidator implements Validator<Integer> {
  @Override
  public void validate(Integer value) {
    if (value <= 0) throw new IllegalArgumentException("Oscars count must be a positive number");
  }
}
