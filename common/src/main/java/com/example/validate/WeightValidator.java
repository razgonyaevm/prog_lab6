package com.example.validate;

/** Валидация веса режиссера */
public class WeightValidator implements Validator<Float> {
  @Override
  public void validate(Float value) {
    if (value <= 0) {
      throw new IllegalArgumentException("Weight must be greater than 0");
    }
  }
}
