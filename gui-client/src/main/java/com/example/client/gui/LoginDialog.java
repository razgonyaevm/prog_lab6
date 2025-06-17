package com.example.client.gui;

import com.example.client.Client;
import java.awt.*;
import javax.swing.*;

public class LoginDialog extends JDialog {
  private JTextField usernameField;
  private JPasswordField passwordField;
  private JButton loginButton;
  private JButton registerButton;
  private Client client;

  public LoginDialog(Frame owner) {
    super(owner, LocalizationManager.getString("login.title"), true);
    client = new Client();
    initComponents();
    setSize(300, 200);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
  }

  private void initComponents() {
    setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    JLabel usernameLabel = new JLabel(LocalizationManager.getString("login.username"));
    gbc.gridx = 0;
    gbc.gridy = 0;
    add(usernameLabel, gbc);

    usernameField = new JTextField(15);
    gbc.gridx = 1;
    gbc.gridy = 0;
    add(usernameField, gbc);

    JLabel passwordLabel = new JLabel(LocalizationManager.getString("login.password"));
    gbc.gridx = 0;
    gbc.gridy = 1;
    add(passwordLabel, gbc);

    passwordField = new JPasswordField(15);
    gbc.gridx = 1;
    gbc.gridy = 1;
    add(passwordField, gbc);

    loginButton = new JButton(LocalizationManager.getString("login.button"));
    gbc.gridx = 0;
    gbc.gridy = 2;
    add(loginButton, gbc);

    registerButton = new JButton(LocalizationManager.getString("register.button"));
    gbc.gridx = 1;
    gbc.gridy = 2;
    add(registerButton, gbc);

    loginButton.addActionListener(e -> performLogin());
    registerButton.addActionListener(e -> performRegister());
  }

  private void performLogin() {
    try {
      if (client.login(usernameField.getText(), new String(passwordField.getPassword()))) {
        dispose();
        new MainFrame(client).setVisible(true);
      } else {
        JOptionPane.showMessageDialog(
            this, "Ошибка авторизации", "Ошибка", JOptionPane.ERROR_MESSAGE);
      }
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void performRegister() {
    try {
      if (client.register(usernameField.getText(), new String(passwordField.getPassword()))) {
        dispose();
        new MainFrame(client).setVisible(true);
      } else {
        JOptionPane.showMessageDialog(
            this, "Ошибка регистрации", "Ошибка", JOptionPane.ERROR_MESSAGE);
      }
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
    }
  }
}
