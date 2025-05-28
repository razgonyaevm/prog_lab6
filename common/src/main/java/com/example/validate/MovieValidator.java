package com.example.validate;

import com.example.service.model.Coordinates;
import com.example.service.model.Movie;

/** Валидация параметров фильма */
public class MovieValidator implements Validator<Movie> {
  private final Validator<String> nameValidator = new NameValidator();
  private final Validator<Coordinates> cordinatesValidator = new CoordinateValidator();
  private final Validator<Integer> oscarsCountValidator = new OscarsCountValidator();
  private final Validator<Integer> lengthValidator = new LengthValidator();

  @Override
  public void validate(Movie movie) {
    nameValidator.validate(movie.getName());
    cordinatesValidator.validate(movie.getCoordinates());
    oscarsCountValidator.validate(movie.getOscarsCount());
    lengthValidator.validate(movie.getLength());
  }
}
