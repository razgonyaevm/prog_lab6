package com.example.config;

import io.github.cdimascio.dotenv.Dotenv;

public class DotenvDatabaseConfig implements DatabaseConfig {
  private final Dotenv dotenv;

  public DotenvDatabaseConfig() {
    this.dotenv = Dotenv.load();
  }

  @Override
  public String getDbUrl() {
    return dotenv.get("DB_URL");
  }

  @Override
  public String getDbUser() {
    return dotenv.get("DB_USER");
  }

  @Override
  public String getDbPassword() {
    return dotenv.get("DB_PASSWORD");
  }
}
