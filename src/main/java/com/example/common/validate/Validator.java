package com.example.common.validate;

/** Интерфейс валидатора */
public interface Validator<T> {
  void validate(T value) throws IllegalArgumentException;
}
