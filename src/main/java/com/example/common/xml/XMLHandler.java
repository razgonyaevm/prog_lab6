package com.example.common.xml;

import com.example.common.service.model.Movie;
import com.example.common.validate.IdsValidator;
import com.example.common.validate.MovieValidator;
import com.example.common.validate.Validator;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

/**
 * Сохранение и загрузка коллекции в XML. Документация взята из <a
 * href="https://javarush.com/quests/lectures/questcollections.level03.lecture07">JavaRush</a>
 */
public class XMLHandler {
  private String filePath;

  private static final Validator<Movie> movieValidator = new MovieValidator();
  private static final Validator<List<Movie>> idsValidator = new IdsValidator();

  public XMLHandler(String filePath) {
    this.filePath = filePath;
  }

  /** Сохранение коллекции в файл (пользователь сам прописывает путь до файла) */
  public void save(LinkedList<Movie> movies) {

    if (filePath.charAt(0) == '~')
      filePath = System.getProperty("user.home") + filePath.substring(1);

    File file = new File(filePath);
    File parent = file.getParentFile();
    if (parent != null) {
      parent.mkdirs();
    }

    try (OutputStream outputStream = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(outputStream);
        OutputStreamWriter osw = new OutputStreamWriter(bos, StandardCharsets.UTF_8)) {

      JAXBContext context = JAXBContext.newInstance(MovieCollectionWrapper.class);
      Marshaller marshaller = context.createMarshaller();

      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      MovieCollectionWrapper wrapper = new MovieCollectionWrapper();
      wrapper.setMovies(movies);

      marshaller.marshal(wrapper, osw);
    } catch (Exception e) {
      System.out.println("Ошибка при сохранении XML: " + e.getMessage());
    }
  }

  /** Загрузка коллекции из локального репозитория */
  public LinkedList<Movie> loadLocal() {
    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      return getMovies(br);
    } catch (FileNotFoundException e) {
      System.out.println("Файл не найден");
    } catch (IOException e) {
      System.out.println("Ошибка при чтении файла: " + e.getMessage());
    } catch (Exception e) {
      System.out.println("Ошибка загрузки XML: " + e.getMessage());
      return new LinkedList<>();
    }
    return new LinkedList<>();
  }

  /** Получение коллекции из BufferedReader */
  private LinkedList<Movie> getMovies(BufferedReader br) throws JAXBException {
    JAXBContext context = JAXBContext.newInstance(MovieCollectionWrapper.class);
    Unmarshaller unmarshaller = context.createUnmarshaller();
    MovieCollectionWrapper wrapper = (MovieCollectionWrapper) unmarshaller.unmarshal(br);

    LinkedList<Movie> movies = wrapper.getMovies();

    // Проверяем уникальность ID и генерируем новые при необходимости
    idsValidator.validate(movies);

    for (Movie movie : movies) {
      movieValidator.validate(movie);
    }
    System.out.println("Коллекция загружена");
    return movies;
  }
}
