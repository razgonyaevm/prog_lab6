package com.example.common.validate;

import com.example.common.service.model.IdGenerator;
import com.example.common.service.model.Movie;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Валидация id фильмов */
public class IdsValidator implements Validator<List<Movie>> {
  @Override
  public void validate(List<Movie> movies) {
    validateIdsExisted(movies);
    long maxId = validateNoDuplicates(movies);
    updateIdGenerator(maxId);
  }

  /** Метод для проверки наличия ID */
  private void validateIdsExisted(List<Movie> movies) {
    long moviesWithoutId = movies.stream().filter(movie -> movie.getId() == null).count();

    if (moviesWithoutId > 0) {
      throw new IllegalArgumentException("Найдены фильмы без ID");
    }
  }

  /** Метод для проверки дубликатов ID */
  private long validateNoDuplicates(List<Movie> movies) {
    Set<Long> uniqueIds = new HashSet<>();
    long maxId = 0;

    for (Movie movie : movies) {
      Long id = movie.getId();

      if (id == null) {
        throw new IllegalArgumentException("Найдены фильмы без ID");
      }

      if (!uniqueIds.add(id)) {
        throw new IllegalArgumentException("Найдены ID, которые дублируют друг друга");
      }

      maxId = Math.max(maxId, id);
    }

    return maxId;
  }

  /** Метод для обновления генератора ID */
  private void updateIdGenerator(long maxId) {
    IdGenerator.reset(maxId + 1);
  }
}
