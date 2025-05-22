package com.example.common.service.model;

import com.example.common.parsing.ParserClass;
import com.example.common.service.enums.Color;
import com.example.common.service.enums.Country;
import com.example.common.validate.HeightValidator;
import com.example.common.validate.NameValidator;
import com.example.common.validate.Validator;
import com.example.common.validate.WeightValidator;
import lombok.*;

import java.io.Serializable;

/** Класс, описывающий человека */
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString
@NoArgsConstructor
public class Person extends ParserClass implements Serializable {
  private String name;
  private Long height;
  private float weight;
  @Setter private Color eyeColor;
  @Setter private Country nationality;

  private static final Validator<String> nameValidator = new NameValidator();
  private static final Validator<Long> heightValidator = new HeightValidator();
  private static final Validator<Float> weightValidator = new WeightValidator();

  public Person(String name, Long height, float weight, Color eyeColor, Country nationality) {
    setName(name);
    setHeight(height);
    setWeight(weight);
    this.eyeColor = eyeColor;
    this.nationality = nationality;
  }

  /** Устанавливает имя */
  public void setName(String name) {
    nameValidator.validate(name);
    this.name = name;
  }

  /** Устанавливает рост */
  public void setHeight(Long height) {
    heightValidator.validate(height);
    this.height = height;
  }

  /** Устанавливает вес */
  public void setWeight(float weight) {
    weightValidator.validate(weight);
    this.weight = weight;
  }
}
