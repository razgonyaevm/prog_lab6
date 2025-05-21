package com.example.service.model;

import com.example.validate.Validator;
import com.example.validate.XValidator;
import com.example.validate.YValidator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** Класс координат (знать бы еще, что это) */
@ToString
@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class Coordinates {
  private double x;
  private Long y;

  private static final Validator<Double> xValidator = new XValidator();
  private static final Validator<Long> yValidator = new YValidator();

  public Coordinates(double x, Long y) {
    setX(x);
    setY(y);
  }

  /** Устанавливает координату X */
  public void setX(double x) {
    xValidator.validate(x);
    this.x = x;
  }

  /** Устанавливает координату Y */
  public void setY(Long y) {
    yValidator.validate(y);
    this.y = y;
  }
}
