package com.example.client.gui;

import com.example.client.Client;
import com.example.network.Response;
import com.example.service.model.Movie;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class MainFrame extends JFrame {
  private static final Logger logger = LogManager.getLogger(MainFrame.class);
  private final Client client;
  private MovieTableModel tableModel;
  private JTable movieTable;
  private VisualizationPanel visualizationPanel;
  private JLabel statusLabel;
  private JMenu fileMenu;
  private JMenu languageMenu;
  private JMenu commandsMenu;
  private JToolBar toolBar;

  public MainFrame(Client client) {
    super();
    this.client = client;
    setTitle(LocalizationManager.getString("app.title"));
    setSize(1200, 800);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
      logger.debug("Установлен Metal Look and Feel");
    } catch (Exception e) {
      logger.error(
          "Ошибка установки Metal Look and Feel: {}, переход на кроссплатформенный",
          e.getMessage(),
          e);
      try {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        logger.debug("Установлен кроссплатформенный Look and Feel");
      } catch (Exception ex) {
        logger.error("Ошибка установки кроссплатформенного Look and Feel: {}", ex.getMessage(), ex);
      }
    }
    initComponents();
    updateLocale();
    refreshTable();
    logger.info("MainFrame инициализирован для пользователя {}", client.getCurrentLogin());
  }

  private void initComponents() {
    logger.debug("Начало инициализации компонентов MainFrame");
    setLayout(new BorderLayout());

    // Меню
    JMenuBar menuBar = new JMenuBar();
    try {
      fileMenu = new JMenu(LocalizationManager.getString("menu.file"));
      JMenuItem exitItem = new JMenuItem(LocalizationManager.getString("menu.file.exit"));
      exitItem.addActionListener(e -> System.exit(0));
      fileMenu.add(exitItem);

      languageMenu = new JMenu(LocalizationManager.getString("menu.language"));
      String[] availableLanguages = getAvailableLanguages();
      for (String lang : availableLanguages) {
        JMenuItem langItem = new JMenuItem(lang);
        langItem.addActionListener(
            e -> {
              LocalizationManager.setLocale(LocalizationManager.getSUPPORTED_LOCALES().get(lang));
              updateLocale();
              logger.info("Язык изменён на: {}", lang);
            });
        languageMenu.add(langItem);
      }
      logger.debug("Языковые элементы меню добавлены: {}", String.join(", ", availableLanguages));

      commandsMenu = new JMenu(LocalizationManager.getString("menu.commands"));
      JMenuItem addItem = new JMenuItem(LocalizationManager.getString("menu.commands.add"));
      addItem.addActionListener(e -> addMovie());
      commandsMenu.add(addItem);

      JMenuItem updateItem = new JMenuItem(LocalizationManager.getString("menu.commands.update"));
      updateItem.addActionListener(e -> updateMovie());
      commandsMenu.add(updateItem);

      JMenuItem removeItem = new JMenuItem(LocalizationManager.getString("menu.commands.remove"));
      removeItem.addActionListener(e -> removeMovie());
      commandsMenu.add(removeItem);

      JMenuItem showItem = new JMenuItem(LocalizationManager.getString("menu.commands.show"));
      showItem.addActionListener(e -> refreshTable());
      commandsMenu.add(showItem);

      JMenuItem infoItem = new JMenuItem(LocalizationManager.getString("menu.commands.info"));
      infoItem.addActionListener(e -> showInfo());
      commandsMenu.add(infoItem);

      JMenuItem clearItem = new JMenuItem(LocalizationManager.getString("menu.commands.clear"));
      clearItem.addActionListener(e -> clearTable());
      commandsMenu.add(clearItem);

      JMenuItem executeScriptItem =
          new JMenuItem(LocalizationManager.getString("menu.commands.execute_script"));
      executeScriptItem.addActionListener(e -> executeScript());
      commandsMenu.add(executeScriptItem);

      JMenuItem removeByIdItem =
          new JMenuItem(LocalizationManager.getString("menu.commands.remove_by_id"));
      removeByIdItem.addActionListener(e -> removeById());
      commandsMenu.add(removeByIdItem);

      JMenuItem removeFirstItem =
          new JMenuItem(LocalizationManager.getString("menu.commands.remove_first"));
      removeFirstItem.addActionListener(e -> removeFirst());
      commandsMenu.add(removeFirstItem);

      JMenuItem reorderItem = new JMenuItem(LocalizationManager.getString("menu.commands.reorder"));
      reorderItem.addActionListener(e -> reorder());
      commandsMenu.add(reorderItem);

      JMenuItem sumOfLengthItem =
          new JMenuItem(LocalizationManager.getString("menu.commands.sum_of_length"));
      sumOfLengthItem.addActionListener(e -> sumOfLength());
      commandsMenu.add(sumOfLengthItem);

      JMenuItem countByOperatorItem =
          new JMenuItem(LocalizationManager.getString("menu.commands.count_by_operator"));
      countByOperatorItem.addActionListener(e -> countByOperator());
      commandsMenu.add(countByOperatorItem);

      JMenuItem printOscarsCountItem =
          new JMenuItem(LocalizationManager.getString("menu.commands.print_oscars_count"));
      printOscarsCountItem.addActionListener(e -> printOscarsCount());
      commandsMenu.add(printOscarsCountItem);

      JMenuItem helpItem = new JMenuItem(LocalizationManager.getString("menu.commands.help"));
      helpItem.addActionListener(e -> showHelp());
      commandsMenu.add(helpItem);

      menuBar.add(fileMenu);
      menuBar.add(languageMenu);
      menuBar.add(commandsMenu);
      setJMenuBar(menuBar);
      logger.debug(
          "Меню инициализированы: fileMenu={}, languageMenu={}, commandsMenu={}",
          fileMenu != null,
          languageMenu != null,
          commandsMenu != null);
    } catch (Exception e) {
      logger.error("Ошибка инициализации меню: {}", e.getMessage(), e);
      JOptionPane.showMessageDialog(
          this,
          "Ошибка инициализации меню: " + e.getMessage(),
          LocalizationManager.getString("error.title"),
          JOptionPane.ERROR_MESSAGE);
    }

    // Панель инструментов
    toolBar = new JToolBar();
    JButton filterButton = new JButton(LocalizationManager.getString("filter.button"));
    filterButton.addActionListener(e -> applyFilter());
    toolBar.add(filterButton);

    JButton sortButton = new JButton(LocalizationManager.getString("sort.button"));
    sortButton.addActionListener(e -> applySort());
    toolBar.add(sortButton);

    add(toolBar, BorderLayout.NORTH);

    // Таблица
    tableModel = new MovieTableModel(new ArrayList<>());
    movieTable = new JTable();
    movieTable.setModel(tableModel);
    movieTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    movieTable.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
              updateMovie();
            }
          }
        });

    JScrollPane tableScrollPane = new JScrollPane(movieTable);

    // Панель визуализации
    visualizationPanel = new VisualizationPanel(client, this);
    JScrollPane visualizationScrollPane = new JScrollPane(visualizationPanel);

    // Разделенная панель
    JSplitPane splitPane =
        new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScrollPane, visualizationScrollPane);
    splitPane.setDividerLocation(600);
    add(splitPane, BorderLayout.CENTER);

    // Статусная строка
    statusLabel =
        new JLabel(LocalizationManager.getString("user.title") + ": " + client.getCurrentLogin());
    add(statusLabel, BorderLayout.SOUTH);

    logger.debug("Компоненты MainFrame инициализированы");
  }

  private String[] getAvailableLanguages() {
    Map<String, Locale> supportedLocales = LocalizationManager.getSUPPORTED_LOCALES();
    if (supportedLocales.isEmpty()) {
      logger.error("SUPPORTED_LOCALES пуст, возвращена заглушка");
      return new String[] {"en"};
    }
    String[] languages = supportedLocales.keySet().toArray(new String[0]);
    logger.debug("Доступные языки для меню: {}", String.join(", ", languages));
    return languages;
  }

  private void updateLocale() {
    logger.debug("Начало обновления локализации");
    setTitle(LocalizationManager.getString("app.title"));
    if (tableModel != null) {
      tableModel.fireTableStructureChanged();
    } else {
      logger.warn("tableModel не инициализирован");
    }
    if (statusLabel != null) {
      statusLabel.setText(
          LocalizationManager.getString("user.title") + ": " + client.getCurrentLogin());
    } else {
      logger.warn("statusLabel не инициализирован");
    }

    if (fileMenu != null) {
      fileMenu.setText(LocalizationManager.getString("menu.file"));
      if (fileMenu.getItemCount() > 0) {
        fileMenu.getItem(0).setText(LocalizationManager.getString("menu.file.exit"));
      }
    } else {
      logger.error("fileMenu не инициализирован");
    }
    if (languageMenu != null) {
      languageMenu.setText(LocalizationManager.getString("menu.language"));
      String currentLang = LocalizationManager.getCurrentLanguageCode();
      logger.debug("Текущий язык: {}", currentLang);
      if (languageMenu.getItemCount() > 0) {
        for (int i = 0; i < languageMenu.getItemCount(); i++) {
          JMenuItem item = languageMenu.getItem(i);
          item.setEnabled(!item.getText().equals(currentLang));
        }
      }
    } else {
      logger.error("languageMenu не инициализирован");
    }
    if (commandsMenu != null) {
      commandsMenu.setText(LocalizationManager.getString("menu.commands"));
      commandsMenu.getItem(0).setText(LocalizationManager.getString("menu.commands.add"));
      commandsMenu.getItem(1).setText(LocalizationManager.getString("menu.commands.update"));
      commandsMenu.getItem(2).setText(LocalizationManager.getString("menu.commands.remove"));
      commandsMenu.getItem(3).setText(LocalizationManager.getString("menu.commands.show"));
      commandsMenu.getItem(4).setText(LocalizationManager.getString("menu.commands.info"));
      commandsMenu.getItem(5).setText(LocalizationManager.getString("menu.commands.clear"));
      commandsMenu
          .getItem(6)
          .setText(LocalizationManager.getString("menu.commands.execute_script"));
      commandsMenu.getItem(7).setText(LocalizationManager.getString("menu.commands.remove_by_id"));
      commandsMenu.getItem(8).setText(LocalizationManager.getString("menu.commands.remove_first"));
      commandsMenu.getItem(9).setText(LocalizationManager.getString("menu.commands.reorder"));
      commandsMenu
          .getItem(10)
          .setText(LocalizationManager.getString("menu.commands.sum_of_length"));
      commandsMenu
          .getItem(11)
          .setText(LocalizationManager.getString("menu.commands.count_by_operator"));
      commandsMenu
          .getItem(12)
          .setText(LocalizationManager.getString("menu.commands.print_oscars_count"));
      commandsMenu.getItem(13).setText(LocalizationManager.getString("menu.commands.help"));
    } else {
      logger.error("commandsMenu не инициализирован");
    }

    if (toolBar != null) {
      ((JButton) toolBar.getComponent(0)).setText(LocalizationManager.getString("filter.button"));
      ((JButton) toolBar.getComponent(1)).setText(LocalizationManager.getString("sort.button"));
    } else {
      logger.error("toolBar не инициализирован");
    }

    for (Window window : Window.getWindows()) {
      if (window instanceof EditMovieDialog dialog) {
        dialog.updateLocale();
      }
    }

    revalidate();
    repaint();
    logger.debug("Локализация обновлена: {}", LocalizationManager.getCurrentLocale());
  }

  public void refreshTable() {
    SwingUtilities.invokeLater(
        () -> {
          try {
            Response response = client.getCollectionResponse();
            List<Movie> movies = new ArrayList<>();
            if (response != null && response.getCollection() != null) {
              movies = response.getCollection().getMovies();
              if (movies == null) {
                movies = new ArrayList<>();
                logger.warn("Список фильмов в коллекции null");
              }
            } else {
              logger.warn("Ответ или коллекция null: response={}", response);
            }

            tableModel.setMovies(movies);
            if (visualizationPanel != null) {
              visualizationPanel.setMovies(movies);
              visualizationPanel.repaint();
            } else {
              logger.error("visualizationPanel не инициализирована");
            }
            movieTable.repaint();

            if (movies.isEmpty()) {
              JOptionPane.showMessageDialog(
                  this,
                  LocalizationManager.getString("empty.collection"),
                  LocalizationManager.getString("info.title"),
                  JOptionPane.INFORMATION_MESSAGE);
            }
            logger.info("Таблица и визуализация обновлены: {} фильмов", movies.size());
          } catch (IOException e) {
            logger.error("Ошибка обновления таблицы: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(
                this,
                LocalizationManager.getString("server.error") + ": " + e.getMessage(),
                LocalizationManager.getString("error.title"),
                JOptionPane.ERROR_MESSAGE);
            tableModel.setMovies(new ArrayList<>());
            if (visualizationPanel != null) {
              visualizationPanel.setMovies(new ArrayList<>());
              visualizationPanel.repaint();
            }
            movieTable.repaint();
          }
        });
  }

  private void addMovie() {
    EditMovieDialog dialog = new EditMovieDialog(this, null, client);
    dialog.setVisible(true);
    if (dialog.isConfirmed()) {
      try {
        Movie movie = dialog.getMovie();
        if (movie == null) {
          logger.error("EditMovieDialog вернул null фильм");
          JOptionPane.showMessageDialog(
              this,
              LocalizationManager.getString("error.invalid.movie"),
              LocalizationManager.getString("error.title"),
              JOptionPane.ERROR_MESSAGE);
          return;
        }
        String response = client.sendCommand("add", movie);
        JOptionPane.showMessageDialog(this, response);
        refreshTable();
        logger.info("Фильм добавлен: {}", movie.getName());
      } catch (Exception e) {
        logger.error("Ошибка добавления фильма: {}", e.getMessage(), e);
        JOptionPane.showMessageDialog(
            this,
            LocalizationManager.getString("server.error") + ": " + e.getMessage(),
            LocalizationManager.getString("error.title"),
            JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void updateMovie() {
    int selectedRow = movieTable.getSelectedRow();
    if (selectedRow >= 0) {
      Movie movie = tableModel.getMovie(selectedRow);
      if (movie.getOwner().getLogin().equals(client.getCurrentLogin())) {
        EditMovieDialog dialog = new EditMovieDialog(this, movie, client);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
          try {
            Movie updatedMovie = dialog.getMovie();
            String response =
                client.sendCommand(
                    "update", new Object[] {updatedMovie, "update " + movie.getId()});
            JOptionPane.showMessageDialog(this, response);
            refreshTable();
            logger.info("Фильм обновлён: id={}", movie.getId());
          } catch (Exception e) {
            logger.error("Ошибка обновления фильма: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(
                this,
                LocalizationManager.getString("server.error") + ": " + e.getMessage(),
                LocalizationManager.getString("error.title"),
                JOptionPane.ERROR_MESSAGE);
          }
        }
      } else {
        logger.warn(
            "Попытка редактирования чужого фильма: id={}, пользователь={}",
            movie.getId(),
            client.getCurrentLogin());
        JOptionPane.showMessageDialog(
            this,
            LocalizationManager.getString("edit.own.error"),
            LocalizationManager.getString("error.title"),
            JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void removeMovie() {
    int selectedRow = movieTable.getSelectedRow();
    if (selectedRow >= 0) {
      Movie movie = tableModel.getMovie(selectedRow);
      if (movie.getOwner().getLogin().equals(client.getCurrentLogin())) {
        int confirm =
            JOptionPane.showConfirmDialog(
                this,
                LocalizationManager.getString("delete.confirm"),
                LocalizationManager.getString("delete.button"),
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
          try {
            String response = client.sendCommand("remove_by_id", "remove_by_id " + movie.getId());
            JOptionPane.showMessageDialog(
                this,
                response,
                LocalizationManager.getString("info.title"),
                JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
            logger.info("Фильм удалён: id={}", movie.getId());
          } catch (Exception e) {
            logger.error("Ошибка удаления фильма: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(
                this,
                LocalizationManager.getString("server.error") + ": " + e.getMessage(),
                LocalizationManager.getString("error.title"),
                JOptionPane.ERROR_MESSAGE);
          }
        }
      } else {
        logger.warn(
            "Попытка удаления чужого фильма: id={}, пользователь={}",
            movie.getId(),
            client.getCurrentLogin());
        JOptionPane.showMessageDialog(
            this,
            LocalizationManager.getString("delete.own.error"),
            LocalizationManager.getString("error.title"),
            JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void applyFilter() {
    String column =
        JOptionPane.showInputDialog(this, LocalizationManager.getString("filter.column.prompt"));
    String value =
        JOptionPane.showInputDialog(this, LocalizationManager.getString("filter.value.prompt"));
    if (column != null && value != null) {
      List<Movie> filtered =
          tableModel.getMovies().stream()
              .filter(
                  movie -> {
                    return switch (column.trim().toLowerCase()) {
                      case "name" -> movie.getName().contains(value);
                      case "oscars" -> movie.getOscarsCount() == Integer.parseInt(value);
                      case "length" -> movie.getLength() == Integer.parseInt(value);
                      case "id" -> movie.getId() == Long.parseLong(value);
                      case "x" -> movie.getCoordinates().getX() == Double.parseDouble(value);
                      case "y" -> movie.getCoordinates().getY() == Long.parseLong(value);
                      case "owner" -> movie.getOwner().getLogin().contains(value);
                      case "operator_name" -> movie.getOperator().getName().contains(value);
                      case "operator_height" ->
                          movie.getOperator().getHeight() == Long.parseLong(value);
                      case "operator_weight" ->
                          movie.getOperator().getWeight() == Float.parseFloat(value);
                      case "operator_color" ->
                          movie.getOperator().getEyeColor().toString().equalsIgnoreCase(value);
                      case "operator_nationality" ->
                          movie.getOperator().getNationality().toString().equalsIgnoreCase(value);
                      case "mpaa" -> movie.getMpaaRating().toString().equalsIgnoreCase(value);
                      case "genre" -> movie.getGenre().toString().equalsIgnoreCase(value);

                      default -> true;
                    };
                  })
              .collect(Collectors.toList());
      tableModel.setMovies(filtered);
      if (visualizationPanel != null) {
        visualizationPanel.setMovies(filtered);
        visualizationPanel.repaint();
      }
      logger.info("Применён фильтр: колонка={}, значение={}", column, value);
    }
  }

  private void applySort() {
    String column =
        JOptionPane.showInputDialog(this, LocalizationManager.getString("sort.column.prompt"));
    if (column != null) {
      List<Movie> sorted =
          tableModel.getMovies().stream()
              .sorted(
                  (m1, m2) -> {
                    return switch (column.trim().toLowerCase()) {
                      case "name" -> m1.getName().compareTo(m2.getName());
                      case "oscars" -> Integer.compare(m1.getOscarsCount(), m2.getOscarsCount());
                      case "length" -> Integer.compare(m1.getLength(), m2.getLength());
                      case "id" -> Long.compare(m1.getId(), m2.getId());
                      case "x" ->
                          Double.compare(m1.getCoordinates().getX(), m2.getCoordinates().getX());
                      case "y" ->
                          Double.compare(m1.getCoordinates().getY(), m2.getCoordinates().getY());
                      case "owner" -> m1.getOwner().getLogin().compareTo(m2.getOwner().getLogin());
                      case "operator_name" ->
                          m1.getOperator().getName().compareTo(m2.getOperator().getName());
                      case "operator_height" ->
                          Long.compare(m1.getOperator().getHeight(), m2.getOperator().getHeight());
                      case "operator_weight" ->
                          Float.compare(m1.getOperator().getWeight(), m2.getOperator().getWeight());
                      case "operator_color" ->
                          m1.getOperator()
                              .getEyeColor()
                              .toString()
                              .compareTo(m2.getOperator().getEyeColor().toString());
                      case "operator_nationality" ->
                          m1.getOperator()
                              .getNationality()
                              .toString()
                              .compareTo(m2.getOperator().getNationality().toString());
                      case "genre" -> m1.getGenre().toString().compareTo(m2.getGenre().toString());
                      case "mpaa" -> m1.getMpaaRating().compareTo(m2.getMpaaRating());
                      case "creation_date" -> m1.getCreationDate().compareTo(m2.getCreationDate());
                      default -> 0;
                    };
                  })
              .collect(Collectors.toList());
      tableModel.setMovies(sorted);
      if (visualizationPanel != null) {
        visualizationPanel.setMovies(sorted);
        visualizationPanel.repaint();
      }
      logger.info("Применена сортировка: колонка={}", column);
    }
  }

  private void showInfo() {
    try {
      String response = client.sendCommand("info", null);
      JOptionPane.showMessageDialog(
          this,
          response,
          LocalizationManager.getString("info.title"),
          JOptionPane.INFORMATION_MESSAGE);
      logger.debug("Запрошена информация о коллекции");
    } catch (IOException e) {
      logger.error("Ошибка получения информации: {}", e.getMessage(), e);
      JOptionPane.showMessageDialog(
          this,
          LocalizationManager.getString("server.error") + ": " + e.getMessage(),
          LocalizationManager.getString("error.title"),
          JOptionPane.ERROR_MESSAGE);
    }
  }

  private void clearTable() {
    tableModel.setMovies(new ArrayList<>());
    try {
      if (visualizationPanel != null) {
        client.sendCommand("clear", null);
        visualizationPanel.setMovies(new ArrayList<>());
        JOptionPane.showMessageDialog(
            this,
            LocalizationManager.getString("collection.cleared"),
            LocalizationManager.getString("info.title"),
            JOptionPane.INFORMATION_MESSAGE);
        refreshTable();
      }

    } catch (IOException e) {
      JOptionPane.showMessageDialog(
          this,
          LocalizationManager.getString("server.error") + ": " + e.getMessage(),
          LocalizationManager.getString("error.title"),
          JOptionPane.ERROR_MESSAGE);
    }
  }

  private void executeScript() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileFilter(
        new javax.swing.filechooser.FileFilter() {
          @Override
          public boolean accept(File f) {
            return true;
          }

          @Override
          public String getDescription() {
            return LocalizationManager.getString("file.filter.all");
          }
        });

    int returnValue = fileChooser.showOpenDialog(this);
    if (returnValue == JFileChooser.APPROVE_OPTION) {
      File selectedFile = fileChooser.getSelectedFile();
      ScriptExecutor executor = new ScriptExecutor(client);
      List<String> results = executor.executeScript(selectedFile.getAbsolutePath());

      JDialog resultsDialog =
          new JDialog(this, LocalizationManager.getString("script.results.title"), true);
      resultsDialog.setLayout(new BorderLayout(10, 10));
      resultsDialog.setSize(600, 400);
      resultsDialog.setLocationRelativeTo(this);

      JTextArea resultsArea = new JTextArea();
      resultsArea.setEditable(false);
      resultsArea.setText(String.join("\n", results));
      JScrollPane scrollPane = new JScrollPane(resultsArea);
      resultsDialog.add(scrollPane, BorderLayout.CENTER);

      JButton closeButton = new JButton(LocalizationManager.getString("close.title"));
      closeButton.addActionListener(e -> resultsDialog.dispose());
      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      buttonPanel.add(closeButton);
      resultsDialog.add(buttonPanel, BorderLayout.SOUTH);

      resultsDialog.setVisible(true);
      refreshTable();
    }
  }

  private void removeById() {
    String idInput =
        JOptionPane.showInputDialog(
            this, LocalizationManager.getString("commands.remove_by_id.prompt"));
    if (idInput != null && !idInput.isEmpty()) {
      try {
        Long.parseLong(idInput.trim());
        String response = client.sendCommand("remove_by_id", "remove_by_id " + idInput.trim());
        JOptionPane.showMessageDialog(
            this,
            response,
            LocalizationManager.getString("info.title"),
            JOptionPane.INFORMATION_MESSAGE);
        refreshTable();
      } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(
            this,
            LocalizationManager.getString("error.invalid.id"),
            LocalizationManager.getString("error.title"),
            JOptionPane.ERROR_MESSAGE);
      } catch (IOException e) {
        JOptionPane.showMessageDialog(
            this,
            LocalizationManager.getString("server.error") + ": " + e.getMessage(),
            LocalizationManager.getString("error.title"),
            JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void removeFirst() {
    try {
      String response = client.sendCommand("remove_first", null);
      JOptionPane.showMessageDialog(
          this,
          response,
          LocalizationManager.getString("info.title"),
          JOptionPane.INFORMATION_MESSAGE);
      refreshTable();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(
          this,
          LocalizationManager.getString("server.error") + ": " + e.getMessage(),
          LocalizationManager.getString("error.title"),
          JOptionPane.ERROR_MESSAGE);
    }
  }

  private void reorder() {
    try {
      String response = client.sendCommand("reorder", null);
      JOptionPane.showMessageDialog(
          this,
          response,
          LocalizationManager.getString("info.title"),
          JOptionPane.INFORMATION_MESSAGE);
      refreshTable();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(
          this,
          LocalizationManager.getString("server.error") + ": " + e.getMessage(),
          LocalizationManager.getString("error.title"),
          JOptionPane.ERROR_MESSAGE);
    }
  }

  private void sumOfLength() {
    try {
      String response = client.sendCommand("sum_of_length", null);
      JOptionPane.showMessageDialog(
          this,
          response,
          LocalizationManager.getString("info.title"),
          JOptionPane.INFORMATION_MESSAGE);
    } catch (IOException e) {
      JOptionPane.showMessageDialog(
          this,
          LocalizationManager.getString("server.error") + ": " + e.getMessage(),
          LocalizationManager.getString("error.title"),
          JOptionPane.ERROR_MESSAGE);
    }
  }

  private void countByOperator() {
    String operatorName =
        JOptionPane.showInputDialog(
            this, LocalizationManager.getString("commands.count_by_operator.prompt"));
    if (operatorName != null && !operatorName.trim().isEmpty()) {
      try {
        String response =
            client.sendCommand("count_by_operator", "count_by_operator " + operatorName.trim());
        JOptionPane.showMessageDialog(
            this,
            response,
            LocalizationManager.getString("info.title"),
            JOptionPane.INFORMATION_MESSAGE);
      } catch (IOException e) {
        JOptionPane.showMessageDialog(
            this,
            LocalizationManager.getString("server.error") + ": " + e.getMessage(),
            LocalizationManager.getString("error.title"),
            JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void printOscarsCount() {
    try {
      String response = client.sendCommand("print_field_descending_oscars_count", null);
      JOptionPane.showMessageDialog(
          this,
          response,
          LocalizationManager.getString("info.title"),
          JOptionPane.INFORMATION_MESSAGE);
    } catch (IOException e) {
      JOptionPane.showMessageDialog(
          this,
          LocalizationManager.getString("server.error") + ": " + e.getMessage(),
          LocalizationManager.getString("error.title"),
          JOptionPane.ERROR_MESSAGE);
    }
  }

  private void showHelp() {
    JDialog helpDialog = new JDialog(this, LocalizationManager.getString("help.title"), true);
    helpDialog.setLayout(new BorderLayout(10, 10));
    helpDialog.setSize(600, 500);
    helpDialog.setLocationRelativeTo(this);

    JEditorPane helpPane = new JEditorPane();
    helpPane.setContentType("text/html");
    helpPane.setEditable(false);

    try {
      String markdown = LocalizationManager.getString("help.content");
      Parser parser = Parser.builder().build();
      Node document = parser.parse(markdown);
      HtmlRenderer renderer = HtmlRenderer.builder().build();
      String htmlContent = renderer.render(document);

      String styleHtml =
          """
              <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 20px; line-height: 1.6; }
                        h1, h2, h3 { color: #333; }
                        h3 { margin-top: 20px; }
                        ul, ol { margin-left: 20px; }
                        p { margin-bottom: 10px; }
                        strong { color: #444; }
                    </style>
                </head>
                <body>
              """
              + htmlContent
              + """
                </body>
                </html>
                """;

      helpPane.setText(styleHtml);
    } catch (Exception e) {
      helpPane.setContentType("text/plain");
      helpPane.setText(LocalizationManager.getString("help.content"));
    }

    JScrollPane scrollPane = new JScrollPane(helpPane);
    helpDialog.add(scrollPane, BorderLayout.CENTER);

    JButton closeButton = new JButton(LocalizationManager.getString("close.title"));
    closeButton.addActionListener(e -> helpDialog.dispose());
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(closeButton);
    helpDialog.add(buttonPanel, BorderLayout.SOUTH);

    helpDialog.setVisible(true);
  }
}
