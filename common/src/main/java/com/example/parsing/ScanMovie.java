package com.example.parsing;

import static com.example.parsing.ParserClass.*;

import com.example.service.enums.MovieGenre;
import com.example.service.enums.MpaaRating;
import com.example.service.model.Coordinates;
import com.example.service.model.Movie;
import java.util.Arrays;
import java.util.Scanner;
import lombok.Getter;

/** Класс для сканирования и создания экземпляров класса {@link Movie} */
public class ScanMovie {
  @Getter private final Movie movie;
  private final Coordinates coordinates;
  private final Scanner scanner;
  private final Boolean executeScript;

  public ScanMovie(Scanner scanner, Boolean executeScript) {
    this.scanner = scanner;
    movie = new Movie();
    coordinates = new Coordinates();
    this.executeScript = executeScript;

    setName();
    setCoordinates();
    setOscarsCount();
    setLength();
    setGenre();
    setMpaaRating();
    ScanOperator operator = new ScanOperator(scanner, executeScript);
    movie.setOperator(operator.getOperator());
  }

  /** Устанавливает название фильма */
  public void setName() {
    while (true) {
      try {
        if (!executeScript) {
          System.out.print("Введите название фильма: ");
        }
        movie.setName(scanner.nextLine());
        break;
      } catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());
      }
    }
  }

  /** Устанавливает координаты фильма (знать бы еще, что это и зачем оно нужно) */
  public void setCoordinates() {
    while (true) {
      try {
        if (!executeScript) {
          System.out.print("Введите первую координату: ");
        }
        String x = scanner.nextLine();
        if (x.trim().isEmpty()) {
          System.out.println("Значение не может быть null");
        } else {
          coordinates.setX(parseDouble(x));
          break;
        }
      } catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());
      }
    }

    while (true) {
      try {
        if (!executeScript) {
          System.out.print("Введите вторую координату: ");
        }
        String y = scanner.nextLine();
        if (y.trim().isEmpty()) {
          System.out.println("Значение не может быть null");
        } else {
          coordinates.setY(parseLong(y));
          break;
        }
      } catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());
      }
    }

    movie.setCoordinates(coordinates);
  }

  /** Устанавливает количество оскаров у фильма */
  public void setOscarsCount() {
    while (true) {
      try {
        if (!executeScript) {
          System.out.print("Введите количество оскаров: ");
        }
        String count = scanner.nextLine();
        if (count.trim().isEmpty()) {
          System.out.println("Значение не может быть null");
        } else {
          movie.setOscarsCount(parseInt(count));
          break;
        }
      } catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());
      }
    }
  }

  /** Устанавливает продолжительность фильма */
  public void setLength() {
    while (true) {
      try {
        if (!executeScript) {
          System.out.print("Введите длительность фильма: ");
        }
        String length = scanner.nextLine();
        if (length.trim().isEmpty()) {
          System.out.println("Значение не может быть null");
        } else {
          movie.setLength(parseInt(length));
          break;
        }
      } catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());
      }
    }
  }

  /** Устанавливает жанр фильма */
  public void setGenre() {
    while (true) {
      try {
        if (!executeScript) {
          System.out.print(
              "Введите жанр фильма (возможные значения: "
                  + Arrays.toString(MovieGenre.values())
                  + "): ");
        }
        String genre = scanner.nextLine().toUpperCase();
        if (genre.trim().isEmpty()) {
          movie.setGenre(null);
        } else {
          movie.setGenre(parseEnum(genre, MovieGenre.class));
        }
        break;
      } catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());
      }
    }
  }

  /** Устанавливает mpaa рейтинг фильма (рейтинг по возрасту) */
  public void setMpaaRating() {
    while (true) {
      try {
        if (!executeScript) {
          System.out.print(
              "Введите рейтинг фильма (возможные значения: "
                  + Arrays.toString(MpaaRating.values())
                  + "): ");
        }

        String mpaaRating = scanner.nextLine().toUpperCase();
        if (mpaaRating.trim().isEmpty()) {
          movie.setMpaaRating(null);
        } else {
          movie.setMpaaRating(parseEnum(mpaaRating, MpaaRating.class));
        }
        break;
      } catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());
      }
    }
  }
}
