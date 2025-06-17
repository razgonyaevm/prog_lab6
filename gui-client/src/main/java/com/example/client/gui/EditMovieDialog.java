package com.example.client.gui;

import com.example.client.Client;
import com.example.service.enums.Color;
import com.example.service.enums.Country;
import com.example.service.enums.MovieGenre;
import com.example.service.enums.MpaaRating;
import com.example.service.model.Coordinates;
import com.example.service.model.Movie;
import com.example.service.model.Operator;
import com.example.service.model.User;
import java.awt.*;
import java.time.ZonedDateTime;
import javax.swing.*;
import lombok.Getter;

public class EditMovieDialog extends JDialog {
  private Movie movie;
  @Getter private boolean confirmed;
  private JTextField nameField, xField, yField, oscarsField, lengthField;
  private JComboBox<MovieGenre> genreCombo;
  private JComboBox<MpaaRating> mpaaCombo;
  private JTextField operatorNameField, heightField, weightField;
  private JComboBox<Color> eyeColorCombo;
  private JComboBox<Country> nationalityCombo;
  private Client client;
  private JButton saveButton;
  private JLabel nameLabel, xLabel, yLabel, oscarsLabel, lengthLabel;
  private JLabel genreLabel, mpaaLabel, operatorNameLabel, heightLabel, weightLabel;
  private JLabel eyeColorLabel, nationalityLabel;

  public EditMovieDialog(MainFrame parent, Movie movie, Client client) {
    super(parent, true);
    this.client = client;
    this.movie = movie;
    this.confirmed = false;
    setSize(400, 600);
    setLocationRelativeTo(parent);
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    initComponents();
    updateLocale();
    if (movie != null) {
      populateFields();
    }
  }

  private void initComponents() {
    setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    int row = 0;
    addField(
        "table.column.name",
        nameLabel = new JLabel(),
        nameField = new JTextField(20),
        gbc,
        0,
        row++);
    addField(
        "table.column.coordinates.x",
        xLabel = new JLabel(),
        xField = new JTextField(20),
        gbc,
        0,
        row++);
    addField(
        "table.column.coordinates.y",
        yLabel = new JLabel(),
        yField = new JTextField(20),
        gbc,
        0,
        row++);
    addField(
        "table.column.oscars",
        oscarsLabel = new JLabel(),
        oscarsField = new JTextField(20),
        gbc,
        0,
        row++);
    addField(
        "table.column.length",
        lengthLabel = new JLabel(),
        lengthField = new JTextField(20),
        gbc,
        0,
        row++);
    addField(
        "table.column.genre",
        genreLabel = new JLabel(),
        genreCombo = new JComboBox<>(MovieGenre.values()),
        gbc,
        0,
        row++);
    addField(
        "table.column.mpaa",
        mpaaLabel = new JLabel(),
        mpaaCombo = new JComboBox<>(MpaaRating.values()),
        gbc,
        0,
        row++);
    addField(
        "operator.name",
        operatorNameLabel = new JLabel(),
        operatorNameField = new JTextField(20),
        gbc,
        0,
        row++);
    addField(
        "operator.height",
        heightLabel = new JLabel(),
        heightField = new JTextField(20),
        gbc,
        0,
        row++);
    addField(
        "operator.weight",
        weightLabel = new JLabel(),
        weightField = new JTextField(20),
        gbc,
        0,
        row++);
    addField(
        "operator.eye_color",
        eyeColorLabel = new JLabel(),
        eyeColorCombo = new JComboBox<>(Color.values()),
        gbc,
        0,
        row++);
    addField(
        "operator.nationality",
        nationalityLabel = new JLabel(),
        nationalityCombo = new JComboBox<>(Country.values()),
        gbc,
        0,
        row++);

    genreCombo.setEnabled(true);
    genreCombo.setFocusable(true);
    mpaaCombo.setEnabled(true);
    eyeColorCombo.setEnabled(true);
    nationalityCombo.setEnabled(true);
    genreCombo.setEditable(false);
    mpaaCombo.setEditable(false);
    eyeColorCombo.setEditable(false);
    nationalityCombo.setEditable(false);

    saveButton = new JButton();
    saveButton.addActionListener(e -> saveMovie());
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 2;
    add(saveButton, gbc);
  }

  private void addField(
      String labelKey, JLabel label, JComponent component, GridBagConstraints gbc, int x, int y) {
    label.setText(LocalizationManager.getString(labelKey));
    gbc.gridx = x;
    gbc.gridy = y;
    gbc.gridwidth = 1;
    add(label, gbc);
    gbc.gridx = x + 1;
    add(component, gbc);
  }

  private void populateFields() {
    if (movie == null) {
      return;
    }
    try {
      nameField.setText(movie.getName());
      xField.setText(LocalizationManager.formatNumber(movie.getCoordinates().getX()));
      yField.setText(LocalizationManager.formatNumber(movie.getCoordinates().getY()));
      oscarsField.setText(LocalizationManager.formatNumber(movie.getOscarsCount()));
      lengthField.setText(LocalizationManager.formatNumber(movie.getLength()));
      genreCombo.setSelectedItem(movie.getGenre());
      mpaaCombo.setSelectedItem(movie.getMpaaRating());
      operatorNameField.setText(movie.getOperator().getName());
      heightField.setText(LocalizationManager.formatNumber(movie.getOperator().getHeight()));
      weightField.setText(LocalizationManager.formatNumber(movie.getOperator().getWeight()));
      eyeColorCombo.setSelectedItem(movie.getOperator().getEyeColor());
      nationalityCombo.setSelectedItem(movie.getOperator().getNationality());
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  private void saveMovie() {
    try {
      validateFields();
      movie = createMovie();
      confirmed = true;
      dispose();
    } catch (IllegalArgumentException ex) {
      JOptionPane.showMessageDialog(
          this,
          LocalizationManager.getString("error.invalid.input") + ": " + ex.getMessage(),
          LocalizationManager.getString("error.title"),
          JOptionPane.ERROR_MESSAGE);
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(
          this,
          LocalizationManager.getString("server.error") + ": " + ex.getMessage(),
          LocalizationManager.getString("error.title"),
          JOptionPane.ERROR_MESSAGE);
    }
  }

  private void validateFields() {
    if (nameField.getText().trim().isEmpty()) {
      throw new IllegalArgumentException("Название фильма не может быть пустым");
    }
    if (operatorNameField.getText().trim().isEmpty()) {
      throw new IllegalArgumentException("Имя оператора не может быть пустым");
    }
    try {
      Double.parseDouble(xField.getText());
      Long.parseLong(yField.getText());
      Integer.parseInt(oscarsField.getText());
      Integer.parseInt(lengthField.getText());
      Long.parseLong(heightField.getText());
      Float.parseFloat(weightField.getText());
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Некорректный формат числовых полей");
    }
    if (genreCombo.getSelectedItem() == null
        || mpaaCombo.getSelectedItem() == null
        || eyeColorCombo.getSelectedItem() == null
        || nationalityCombo.getSelectedItem() == null) {
      throw new IllegalArgumentException("Все выпадающие списки должны быть выбраны");
    }
  }

  private Movie createMovie() {
    Movie newMovie = new Movie();
    newMovie.setName(nameField.getText().trim());
    newMovie.setCoordinates(
        new Coordinates(Double.parseDouble(xField.getText()), Long.parseLong(yField.getText())));
    newMovie.setOscarsCount(Integer.parseInt(oscarsField.getText()));
    newMovie.setLength(Integer.parseInt(lengthField.getText()));
    newMovie.setGenre((MovieGenre) genreCombo.getSelectedItem());
    newMovie.setMpaaRating((MpaaRating) mpaaCombo.getSelectedItem());
    newMovie.setOperator(
        new Operator(
            operatorNameField.getText().trim(),
            Long.parseLong(heightField.getText()),
            Float.parseFloat(weightField.getText()),
            (Color) eyeColorCombo.getSelectedItem(),
            (Country) nationalityCombo.getSelectedItem()));
    newMovie.setOwner(new User(0, client.getCurrentLogin()));
    newMovie.setCreationDate(ZonedDateTime.now().toLocalDate());
    return newMovie;
  }

  public Movie getMovie() {
    return confirmed ? movie : null;
  }

  public void updateLocale() {
    setTitle(
        LocalizationManager.getString(movie == null ? "dialog.add.title" : "dialog.edit.title"));
    nameLabel.setText(LocalizationManager.getString("table.column.name"));
    xLabel.setText(LocalizationManager.getString("table.column.coordinates.x"));
    yLabel.setText(LocalizationManager.getString("table.column.coordinates.y"));
    oscarsLabel.setText(LocalizationManager.getString("table.column.oscars"));
    lengthLabel.setText(LocalizationManager.getString("table.column.length"));
    genreLabel.setText(LocalizationManager.getString("table.column.genre"));
    mpaaLabel.setText(LocalizationManager.getString("table.column.mpaa"));
    operatorNameLabel.setText(LocalizationManager.getString("operator.name"));
    heightLabel.setText(LocalizationManager.getString("operator.height"));
    weightLabel.setText(LocalizationManager.getString("operator.weight"));
    eyeColorLabel.setText(LocalizationManager.getString("operator.eye_color"));
    nationalityLabel.setText(LocalizationManager.getString("operator.nationality"));
    saveButton.setText(LocalizationManager.getString("save.title"));
    revalidate();
    repaint();
  }
}
