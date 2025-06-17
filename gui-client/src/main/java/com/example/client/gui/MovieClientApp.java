package com.example.client.gui;

import javax.swing.*;

public class MovieClientApp {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(
        () -> {
          LoginDialog loginDialog = new LoginDialog(null);
          loginDialog.setVisible(true);
        });
  }
}
