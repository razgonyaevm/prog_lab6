package com.example.parsing;

/** Парсинг базовых параметров */
public class ParserClass {
  /** Преобразование строки в число int */
  public static int parseInt(String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid integer: " + value, e);
    }
  }

  /** Преобразование строки в число long */
  public static long parseLong(String value) {
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid long: " + value, e);
    }
  }

  /** Преобразование строки в число float */
  public static float parseFloat(String value) {
    try {
      return Float.parseFloat(value);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid float: " + value, e);
    }
  }

  /** Преобразование строки в число double */
  public static double parseDouble(String value) {
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid double: " + value, e);
    }
  }

  /** Преобразование строки в enum с заданным типом */
  public static <E extends Enum<E>> E parseEnum(String value, Class<E> enumType) {
    try {
      return Enum.valueOf(enumType, value);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "Invalid enum value: " + value + " for " + enumType.getSimpleName(), e);
    }
  }
}
