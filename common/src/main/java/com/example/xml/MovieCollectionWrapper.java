package com.example.xml;

import com.example.service.model.Movie;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.LinkedList;
import java.util.List;
import lombok.Setter;

/** Класс-обертка для реализации Movie в XML */
@Setter
@XmlRootElement(name = "movies")
@XmlAccessorType(XmlAccessType.FIELD) // Аннатация для работы JAXB с полями
public class MovieCollectionWrapper {
  @XmlElement(name = "movie")
  private List<Movie> movies = new LinkedList<>();

  /** Геттер коллекции */
  public LinkedList<Movie> getMovies() {
    return (LinkedList<Movie>) movies;
  }
}
