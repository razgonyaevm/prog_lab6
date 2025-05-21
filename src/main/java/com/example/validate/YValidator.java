package com.example.validate;

/** Валидация параметра Y в {@link com.example.service.model.Coordinates} */
public class YValidator implements Validator<Long> {
  @Override
  public void validate(Long value) {
    if (value == null) throw new IllegalArgumentException("Y cannot be null");
  }
}
