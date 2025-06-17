package com.example.client.gui;

import com.example.service.model.Movie;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import lombok.Getter;

public class MovieTableModel extends AbstractTableModel {
  @Getter private List<Movie> movies;
  private final String[] columnNames = {
    LocalizationManager.getString("table.column.id"),
    LocalizationManager.getString("table.column.name"),
    LocalizationManager.getString("table.column.length"),
    LocalizationManager.getString("table.column.coordinates.x"),
    LocalizationManager.getString("table.column.coordinates.y"),
    LocalizationManager.getString("table.column.oscars"),
    LocalizationManager.getString("table.column.genre"),
    LocalizationManager.getString("table.column.mpaa"),
    LocalizationManager.getString("table.column.operator"),
    LocalizationManager.getString("table.column.owner"),
    LocalizationManager.getString("table.column.created")
  };

  public MovieTableModel(List<Movie> movies) {
    this.movies = new LinkedList<>(movies);
  }

  public void setMovies(List<Movie> movies) {
    this.movies = new LinkedList<>(movies);
    fireTableDataChanged();
  }

  public Movie getMovie(int row) {
    return movies.get(row);
  }

  @Override
  public int getRowCount() {
    return movies.size();
  }

  @Override
  public int getColumnCount() {
    return columnNames.length;
  }

  @Override
  public String getColumnName(int column) {
    return columnNames[column];
  }

  @Override
  public Object getValueAt(int row, int column) {
    Movie movie = movies.get(row);
    return switch (column) {
      case 0 -> movie.getId();
      case 1 -> movie.getName();
      case 2 -> LocalizationManager.formatNumber(movie.getLength());
      case 3 -> LocalizationManager.formatNumber(movie.getCoordinates().getX());
      case 4 -> LocalizationManager.formatNumber(movie.getCoordinates().getY());
      case 5 -> LocalizationManager.formatNumber(movie.getOscarsCount());
      case 6 -> movie.getGenre();
      case 7 -> movie.getMpaaRating();
      case 8 -> movie.getOperator().getName();
      case 9 -> movie.getOwner().getLogin();
      case 10 -> LocalizationManager.formatDate(movie.getCreationDate().atStartOfDay());
      default -> null;
    };
  }
}
