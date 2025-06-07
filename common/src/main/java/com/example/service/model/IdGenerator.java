package com.example.service.model;

/** Класс генерации id для {@link Movie} */
public class IdGenerator {
  private static long nextId = 1;

  /** Увеличивает id на 1 */
  public static long getNextId() {
    return nextId++;
  }

  /** Сбрасывает генератор на указанное значение */
  public static void reset(long newStartValue) {
    nextId = newStartValue;
  }
}
