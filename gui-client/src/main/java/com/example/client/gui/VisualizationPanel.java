package com.example.client.gui;

import com.example.client.Client;
import com.example.service.model.Movie;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.*;
import javax.swing.Timer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VisualizationPanel extends JPanel {
  private static final Logger logger = LogManager.getLogger(VisualizationPanel.class);
  private List<Movie> movies;
  private final Client client;
  private final MainFrame mainFrame;
  private final Map<String, Color> userColors; // Цвета для пользователей
  private final Map<Long, Float>
      animationScales; // Масштаб анимации (0.0f для новых, 1.0f для существующих, <0 для удаляемых)
  private static final int ANIMATION_DURATION = 1000; // Длительность анимации (мс)
  private static final int ANIMATION_STEPS = 60; // Количество шагов анимации
  private static final int GAP = 4;
  private static final int MAX_ITERATIONS = 200;
  private Timer animationTimer;

  public VisualizationPanel(Client client, MainFrame mainFrame) {
    this.client = client;
    this.mainFrame = mainFrame;
    this.movies = new ArrayList<>();
    this.userColors = new ConcurrentHashMap<>();
    this.animationScales = new ConcurrentHashMap<>();
    setBackground(Color.WHITE);
    initMouseListener();
    initAnimationTimer();
    logger.debug("VisualizationPanel инициализирована");
  }

  private void initMouseListener() {
    addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            Movie clickedMovie = getMovieAt(e.getX(), e.getY());
            if (clickedMovie != null) {
              showMovieDialog(clickedMovie);
              logger.info(
                  "Клик по фильму: id={}, name={}", clickedMovie.getId(), clickedMovie.getName());
            } else {
              logger.debug("Клик вне фильма: x={}, y={}", e.getX(), e.getY());
            }
          }
        });
  }

  private void initAnimationTimer() {
    animationTimer =
        new Timer(
            ANIMATION_DURATION / ANIMATION_STEPS,
            e -> {
              boolean hasActiveAnimations = false;
              synchronized (animationScales) {
                for (Iterator<Map.Entry<Long, Float>> it = animationScales.entrySet().iterator();
                    it.hasNext(); ) {
                  Map.Entry<Long, Float> entry = it.next();
                  float scale = entry.getValue();
                  if (scale < 0.0f) {
                    // Анимация удаления (уменьшение)
                    scale += 1.0f / ANIMATION_STEPS;
                    if (scale >= 0.0f) {
                      it.remove();
                    } else {
                      entry.setValue(scale);
                      hasActiveAnimations = true;
                    }
                  } else {
                    // Анимация появления (увеличение)
                    scale += 1.0f / ANIMATION_STEPS;
                    if (scale >= 1.0f) {
                      it.remove();
                    } else {
                      entry.setValue(scale);
                      hasActiveAnimations = true;
                    }
                  }
                }
              }
              if (!hasActiveAnimations) {
                animationTimer.stop();
                logger.debug("Анимация завершена");
              }
              repaint();
              logger.trace(
                  "Перерисовка для анимации: активных анимаций={}", animationScales.size());
            });
  }

  public void setMovies(List<Movie> newMovies) {
    synchronized (this) {
      if (newMovies == null) {
        this.movies = new ArrayList<>();
        logger.warn("Передан null список фильмов");
        return;
      }

      // Определяем новые и удалённые фильмы
      List<Long> currentIds = movies.stream().map(Movie::getId).filter(Objects::nonNull).toList();
      List<Long> newIds = newMovies.stream().map(Movie::getId).filter(Objects::nonNull).toList();

      // Новые фильмы (появление)
      for (Movie movie : newMovies) {
        if (movie.getId() == null) {
          logger.warn("Фильм без ID: name={}", movie.getName());
          continue;
        }
        if (!currentIds.contains(movie.getId())) {
          synchronized (animationScales) {
            animationScales.put(movie.getId(), 0.0f);
          }
          logger.debug("Добавлена анимация появления для фильма: id={}", movie.getId());
        }
      }

      // Удалённые фильмы (исчезновение)
      for (Movie movie : movies) {
        if (movie.getId() == null) {
          continue;
        }
        if (!newIds.contains(movie.getId())) {
          synchronized (animationScales) {
            animationScales.put(movie.getId(), -1.0f);
          }
          logger.debug("Добавлена анимация исчезновения для фильма: id={}", movie.getId());
        }
      }

      this.movies = new ArrayList<>(newMovies);
      if (!animationScales.isEmpty() && !animationTimer.isRunning()) {
        animationTimer.start();
        logger.debug("Запущена анимация для {} фильмов", animationScales.size());
      }
      logger.debug("Установлено {} фильмов для визуализации", movies.size());
      repaint();
    }
  }

  private Color getColorForUser(String userLogin) {
    return userColors.computeIfAbsent(
        userLogin,
        k -> {
          Random rand = new Random(k.hashCode());
          Color color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
          logger.debug(
              "Назначен цвет для пользователя {}: RGB({}, {}, {})",
              k,
              color.getRed(),
              color.getGreen(),
              color.getBlue());
          return color;
        });
  }

  private Movie getMovieAt(int x, int y) {
    List<Square> squares = calculateSquarePositions();
    for (Square square : squares) {
      if (x >= square.x - (double) square.size / 2
          && x <= square.x + (double) square.size / 2
          && y >= square.y - (double) square.size / 2
          && y <= square.y + (double) square.size / 2) {
        return square.movie;
      }
    }
    return null;
  }

  private void showMovieDialog(Movie movie) {
    JDialog dialog =
        new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            LocalizationManager.getString("object.info.title"),
            true);
    dialog.setLayout(new BorderLayout(10, 10));
    dialog.setSize(400, 400);
    dialog.setLocationRelativeTo(this);
    String info =
        String.format(
                "<html><b>%s:</b> %s<br>",
                LocalizationManager.getString("table.column.name"), movie.getName())
            + String.format(
                "<b>%s:</b> %.2f<br>",
                LocalizationManager.getString("table.column.coordinates.x"),
                movie.getCoordinates().getX())
            + String.format(
                "<b>%s:</b> %d<br>",
                LocalizationManager.getString("table.column.coordinates.y"),
                movie.getCoordinates().getY())
            + String.format(
                "<b>%s:</b> %d<br>",
                LocalizationManager.getString("table.column.oscars"), movie.getOscarsCount())
            + String.format(
                "<b>%s:</b> %d<br>",
                LocalizationManager.getString("table.column.length"), movie.getLength())
            + String.format(
                "<b>%s:</b> %s<br>",
                LocalizationManager.getString("table.column.genre"), movie.getGenre())
            + String.format(
                "<b>%s:</b> %s<br>",
                LocalizationManager.getString("table.column.mpaa"), movie.getMpaaRating())
            + String.format(
                "<b>%s:</b> %s<br>",
                LocalizationManager.getString("operator.name"), movie.getOperator().getName())
            + String.format(
                "<b>%s:</b> %d<br>",
                LocalizationManager.getString("operator.height"), movie.getOperator().getHeight())
            + String.format(
                "<b>%s:</b> %.2f<br>",
                LocalizationManager.getString("operator.weight"), movie.getOperator().getWeight())
            + String.format(
                "<b>%s:</b> %s<br>",
                LocalizationManager.getString("operator.eye_color"),
                movie.getOperator().getEyeColor())
            + String.format(
                "<b>%s:</b> %s<br>",
                LocalizationManager.getString("operator.nationality"),
                movie.getOperator().getNationality())
            + String.format(
                "<b>%s:</b> %s</html>",
                LocalizationManager.getString("user.title"), movie.getOwner().getLogin());

    JTextPane infoPane = new JTextPane();
    infoPane.setContentType("text/html");
    infoPane.setText(info);
    infoPane.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(infoPane);
    dialog.add(scrollPane, BorderLayout.CENTER);

    // Панель с кнопками
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton closeButton = new JButton(LocalizationManager.getString("close.title"));
    closeButton.addActionListener(e -> dialog.dispose());
    buttonPanel.add(closeButton);

    // Кнопка обновить
    if (movie.getOwner().getLogin().equals(client.getCurrentLogin())) {
      JButton updateButton = new JButton(LocalizationManager.getString("menu.commands.update"));
      updateButton.addActionListener(
          e -> {
            dialog.dispose();
            updateMovie(movie);
          });
      buttonPanel.add(updateButton);
    }

    dialog.add(buttonPanel, BorderLayout.SOUTH);
    dialog.setVisible(true);
  }

  private void updateMovie(Movie movie) {
    EditMovieDialog editDialog =
        new EditMovieDialog((MainFrame) SwingUtilities.getWindowAncestor(this), movie, client);
    editDialog.setVisible(true);
    if (editDialog.isConfirmed()) {
      try {
        Movie updatedMovie = editDialog.getMovie();
        client.sendCommand("update", new Object[] {updatedMovie, "update " + movie.getId()});
        // обновление визуализации
        List<Movie> updatedMovies = client.getCollectionResponse().getCollection().getMovies();
        setMovies(updatedMovies);
        mainFrame.refreshTable();
        JOptionPane.showMessageDialog(
            this,
            LocalizationManager.getString("collection.updated"),
            LocalizationManager.getString("info.title"),
            JOptionPane.INFORMATION_MESSAGE);
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(
            this,
            LocalizationManager.getString("server.error") + ": " + ex.getMessage(),
            LocalizationManager.getString("error.title"),
            JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private static class Square {
    Movie movie;
    double x, y;
    int size;

    Square(Movie movie, double x, double y, int size) {
      this.movie = movie;
      this.x = x;
      this.y = y;
      this.size = size;
    }
  }

  private List<Square> calculateSquarePositions() {
    List<Square> squares = new ArrayList<>();
    if (movies == null || movies.isEmpty()) {
      return squares;
    }

    double minX = movies.stream().mapToDouble(m -> m.getCoordinates().getX()).min().orElse(0);
    double maxX = movies.stream().mapToDouble(m -> m.getCoordinates().getX()).max().orElse(1);
    double minY = movies.stream().mapToDouble(m -> m.getCoordinates().getY()).min().orElse(0);
    double maxY = movies.stream().mapToDouble(m -> m.getCoordinates().getY()).max().orElse(1);

    int padding = 50;
    int width = getWidth() - 2 * padding;
    int height = getHeight() - 2 * padding;
    double scaleX = width / (maxX - minX + 0.0001);
    double scaleY = height / (maxY - minY + 0.0001);

    for (Movie movie : movies) {
      if (movie.getCoordinates() == null) {
        continue;
      }
      double x = (movie.getCoordinates().getX() - minX) * scaleX + padding;
      double y = (movie.getCoordinates().getY() - minY) * scaleY + padding;
      int baseSize = 10 + movie.getOscarsCount() * 2;
      float animScale = animationScales.getOrDefault(movie.getId(), 1.0f);
      if (animScale <= 0.0f && animScale > -1.0f) {
        continue;
      }
      int size = (int) (baseSize * Math.abs(animScale));
      squares.add(new Square(movie, x, y, size));
    }

    // Коррекция позиций для избежания пересечений
    boolean hasOverlaps;
    int iterations = 0;
    Random rand = new Random();
    do {
      hasOverlaps = false;
      for (int i = 0; i < squares.size(); i++) {
        Square s1 = squares.get(i);
        for (int j = i + 1; j < squares.size(); j++) {
          Square s2 = squares.get(j);
          double dx = s1.x - s2.x;
          double dy = s1.y - s2.y;
          double distance = Math.sqrt(dx * dx + dy * dy);
          double minDistance = Math.sqrt(2) * (s1.size + s2.size) / 2 + GAP;

          if (distance < minDistance) {
            hasOverlaps = true;
            double overlap = (minDistance - distance) / 2;
            if (distance < 0.0001) {
              double angle = rand.nextDouble() * 2 + Math.PI;
              double moveX = Math.cos(angle) * minDistance / 2;
              double moveY = Math.sin(angle) * minDistance / 2;
              s1.x += moveX;
              s1.y += moveY;
              s2.x -= moveX;
              s2.y -= moveY;
            } else {
              double moveX = (dx / distance) * overlap;
              double moveY = (dy / distance) * overlap;

              s1.x += moveX;
              s1.y += moveY;
              s2.x -= moveX;
              s2.y -= moveY;

              logger.trace(
                  "Разделены квадраты: id1={}, id2={}, moveX={}, moveY={}",
                  s1.movie.getId(),
                  s2.movie.getId(),
                  moveX,
                  moveY);
            }
          }
        }
      }

      for (Square square : squares) {
        int halfSize = square.size / 2;
        square.x = Math.max(padding + halfSize, Math.min(width + padding - halfSize, square.x));
        square.y = Math.max(padding + halfSize, Math.min(height + padding - halfSize, square.y));
        if (square.x == padding + halfSize
            || square.x == width + padding - halfSize
            || square.y == padding + halfSize
            || square.y == height + padding - halfSize) {
          logger.trace(
              "Квадрат ограничен границами: ID={}, x={}, y={}\",\n",
              square.movie.getId(),
              square.x,
              square.y);
        }
      }
      iterations++;
    } while (hasOverlaps && iterations < MAX_ITERATIONS);

    return squares;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    if (movies == null || movies.isEmpty()) {
      g2d.setColor(Color.BLACK);
      g2d.drawString(LocalizationManager.getString("empty.collection"), 50, 50);
      logger.debug("Коллекция пуста, отображено сообщение");
      return;
    }

    List<Square> squares = calculateSquarePositions();

    for (Square square : squares) {
      Movie movie = square.movie;
      double x = square.x;
      double y = square.y;
      int size = square.size;
      int halfSize = size / 2;

      g2d.setColor(getColorForUser(movie.getOwner().getLogin()));
      g2d.fillRect((int) x - halfSize, (int) y - halfSize, size, size);

      if (movie.getOwner().getLogin().equals(client.getCurrentLogin())) {
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect((int) x - halfSize, (int) y - halfSize, size, size);
      }
    }
    logger.debug("Нарисовано {} фильмов", movies.size());
  }
}
