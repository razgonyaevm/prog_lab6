package com.example.validate;

import com.example.service.model.Coordinates;

/** Валидация координат */
public class CoordinateValidator implements Validator<Coordinates> {
  private final Validator<Double> xValidate = new XValidator();
  private final Validator<Long> yValidate = new YValidator();

  @Override
  public void validate(Coordinates value) {
    if (value == null) {
      throw new IllegalArgumentException("Movie coordinates cannot be null");
    }
    xValidate.validate(value.getX());
    yValidate.validate(value.getY());
  }
}
