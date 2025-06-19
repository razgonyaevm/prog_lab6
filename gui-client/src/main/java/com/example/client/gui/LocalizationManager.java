package com.example.client.gui;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.ResourceBundle;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LocalizationManager {
  private static final Logger logger = LogManager.getLogger(LocalizationManager.class);
  @Getter private static final Map<String, Locale> SUPPORTED_LOCALES = new HashMap<>();
  @Getter private static Locale currentLocale = new Locale("ru", "RU");
  private static ResourceBundle messages;
  private static NumberFormat numberFormat;
  private static DateFormat dateFormat;
  private static DateFormat timeFormat;

  static {
    logger.info("Системная локаль JVM: {}", Locale.getDefault());
    logger.info("Системная локаль ОС: {}", System.getenv("LANG"));
    Locale.setDefault(new Locale("ru", "RU"));
    SUPPORTED_LOCALES.put("Русский", new Locale("ru", "RU"));
    SUPPORTED_LOCALES.put("Українська", new Locale("uk", "UA"));
    SUPPORTED_LOCALES.put("Suomi", new Locale("fi", "FI"));
    SUPPORTED_LOCALES.put("English (South Africa)", new Locale("en", "ZA"));
    logger.debug("SUPPORTED_LOCALES инициализированы: {}", SUPPORTED_LOCALES.keySet());
    ResourceBundle.clearCache(LocalizationManager.class.getClassLoader());
    logger.debug("Кэш ResourceBundle очищен");
    setLocale(new Locale("ru", "RU"));
    loadMessages();
    logger.info("LocalizationManager инициализирован с локалью: {}", currentLocale);
  }

  public static void setLocale(Locale locale) {
    if (locale != null && SUPPORTED_LOCALES.containsValue(locale)) {
      currentLocale = locale;
      ResourceBundle.clearCache(LocalizationManager.class.getClassLoader());
      try {
        ClassLoader loader = LocalizationManager.class.getClassLoader();
        String resourceName =
            "com/example/client/gui/Messages_"
                + locale.getLanguage()
                + "_"
                + locale.getCountry()
                + ".properties";
        if (loader.getResource(resourceName) == null) {
          logger.warn(
              "Файл {} не найден, остаёмся на текущей локали {}", resourceName, currentLocale);
          return;
        }
        messages = ResourceBundle.getBundle("com.example.client.gui.Messages", locale);
        numberFormat = NumberFormat.getInstance(locale);
        dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);
        logger.debug("Локаль установлена: {}, ресурсы загружены", locale);
      } catch (Exception e) {
        logger.error("Ошибка установки локали {}: {}, остаёмся на ru", locale, e.getMessage(), e);
        currentLocale = new Locale("ru", "RU");
        messages = ResourceBundle.getBundle("com.example.client.gui.Messages", currentLocale);
        numberFormat = NumberFormat.getInstance(currentLocale);
        dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, currentLocale);
        timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM, currentLocale);
      }
    } else {
      logger.warn(
          "Попытка установить неподдерживаемую локаль: {}, остаёмся на {}", locale, currentLocale);
    }
  }

  public static String getString(String key) {
    try {
      String value = messages.getString(key);
      logger.trace("Ключ {} возвратил значение: {}", key, value);
      return value;
    } catch (Exception e) {
      logger.warn("Ключ {} не найден в локали {}, возвращён ключ", key, currentLocale);
      return key;
    }
  }

  public static String getCurrentLanguageCode() {
    for (Map.Entry<String, Locale> entry : SUPPORTED_LOCALES.entrySet()) {
      if (entry.getValue().equals(currentLocale)) {
        return entry.getKey();
      }
    }
    logger.warn(
        "Текущая локаль {} не найдена в SUPPORTED_LOCALES, возвращён Русский", currentLocale);
    return "Русский";
  }

  public static String formatNumber(Number number) {
    return numberFormat.format(number);
  }

  public static String formatDate(LocalDateTime dateTime) {
    if (dateTime == null) return "";
    Date date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    return dateFormat.format(date) + " " + timeFormat.format(date);
  }

  public static Number parseNumber(String numberStr) throws ParseException {
    return numberFormat.parse(numberStr);
  }

  private static void loadMessages() {
    try {
      ClassLoader loader = LocalizationManager.class.getClassLoader();
      String resourceName =
          "com/example/client/gui/Messages_"
              + currentLocale.getLanguage()
              + "_"
              + currentLocale.getCountry()
              + ".properties";
      if (loader.getResource(resourceName) == null) {
        logger.error("Файл {} не найден, переход на ru", resourceName);
        currentLocale = new Locale("ru", "RU");
      }
      ResourceBundle.Control control =
          ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);
      messages =
          ResourceBundle.getBundle("com.example.client.gui.Messages", currentLocale, control);
      logger.debug("Ресурсы загружены для локали: {}", currentLocale);
    } catch (Exception e) {
      logger.error(
          "Ошибка загрузки ресурсов для локали {}: {}, переход на ru",
          currentLocale,
          e.getMessage(),
          e);
      currentLocale = new Locale("ru", "RU");
      ResourceBundle.clearCache(LocalizationManager.class.getClassLoader());
      messages = ResourceBundle.getBundle("com.example.client.gui.Messages", currentLocale);
    }
  }
}
