package com.example.service.model;

import com.example.parsing.ParserClass;
import com.example.service.enums.Color;
import com.example.service.enums.Country;
import com.example.validate.HeightValidator;
import com.example.validate.NameValidator;
import com.example.validate.Validator;
import com.example.validate.WeightValidator;
import java.io.Serializable;
import lombok.*;

/** Класс, описывающий человека */
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString
@NoArgsConstructor
public class Operator extends ParserClass implements Serializable {
  @Setter private Long id;
  private String name;
  private Long height;
  private float weight;
  @Setter private Color eyeColor;
  @Setter private Country nationality;

  private static final Validator<String> nameValidator = new NameValidator();
  private static final Validator<Long> heightValidator = new HeightValidator();
  private static final Validator<Float> weightValidator = new WeightValidator();

  public Operator(String name, Long height, float weight, Color eyeColor, Country nationality) {
    setName(name);
    setHeight(height);
    setWeight(weight);
    this.eyeColor = eyeColor;
    this.nationality = nationality;
  }

  public void generateId() {
    this.id = IdGenerator.getNextId();
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
