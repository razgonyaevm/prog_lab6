package com.example.validate;

import com.example.service.model.Coordinates;

/** Валидация параметра Y в {@link Coordinates} */
public class YValidator implements Validator<Long> {
  @Override
  public void validate(Long value) {
    if (value == null) throw new IllegalArgumentException("Y cannot be null");
  }
}
