package com.example.parsing;

import static com.example.parsing.ParserClass.*;

import com.example.service.enums.Color;
import com.example.service.enums.Country;
import com.example.service.model.Operator;
import java.util.Arrays;
import java.util.Scanner;
import lombok.Getter;

/** Класс для сканирования оператора в виде объекта класса {@link Operator} */
public class ScanOperator {
  private final Scanner scanner;
  @Getter private final Operator operator;
  private final Boolean executeScript;

  public ScanOperator(Scanner scanner, Boolean executeScript) {
    this.scanner = scanner;
    operator = new Operator();
    this.executeScript = executeScript;
    setOperator();
    operator.generateId();
  }

  public void setOperator() {
    while (true) {
      try {
        if (!executeScript) {
          System.out.print("Введите имя оператора: ");
        }
        operator.setName(scanner.nextLine());
        break;
      } catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());
      }
    }

    while (true) {
      try {
        if (!executeScript) {
          System.out.print("Введите рост оператора: ");
        }
        String height = scanner.nextLine();
        if (height.trim().isEmpty()) {
          System.out.println("Значение не может быть null");
        } else {
          operator.setHeight(parseLong(height));
          break;
        }
      } catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());
      }
    }

    while (true) {
      try {
        if (!executeScript) {
          System.out.print("Введите вес оператора: ");
        }
        String weight = scanner.nextLine();
        if (weight.trim().isEmpty()) {
          operator.setWeight(0);
        } else {
          operator.setWeight(parseFloat(weight));
        }
        break;
      } catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());
      }
    }

    while (true) {
      try {
        if (!executeScript) {
          System.out.println(
              "Введите любимый цвет оператора: (возможные значения: "
                  + Arrays.toString(Color.values())
                  + "): ");
        }
        String color = scanner.nextLine().toUpperCase();
        if (color.trim().isEmpty()) {
          operator.setEyeColor(null);
        } else {
          operator.setEyeColor(parseEnum(color, Color.class));
        }
        break;
      } catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());
      }
    }

    while (true) {
      try {
        if (!executeScript) {
          System.out.println(
              "Введите национальность оператора (возможные значения: "
                  + Arrays.toString(Country.values())
                  + "): ");
        }
        String nationality = scanner.nextLine().toUpperCase();
        if (nationality.trim().isEmpty()) {
          operator.setNationality(null);
        } else {
          operator.setNationality(parseEnum(nationality, Country.class));
        }
        break;
      } catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());
      }
    }
  }
}
