package com.example.common.validate;

import com.example.common.service.model.Coordinates;

/** Валидация параметра X в {@link Coordinates} */
public class XValidator implements Validator<Double> {
  @Override
  public void validate(Double value) {
    if (value <= -817) {
      throw new IllegalArgumentException("X must be greater than -817");
    }
  }
}
