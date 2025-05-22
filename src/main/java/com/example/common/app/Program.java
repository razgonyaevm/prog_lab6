package com.example.common.app;

import static com.example.common.app.commandhandling.CommandHandler.handleCommand;

import com.example.common.app.commandhandling.CommandInvoker;
import com.example.common.app.commandhandling.commands.*;
import com.example.common.service.MovieCollection;
import com.example.common.xml.XMLHandler;
import java.util.Scanner;

/** Основная программа. При запуске аргументом указывается имя xml-файла */
public class Program {
  public static void main(String[] args) {
    MovieCollection collection = new MovieCollection();
    if (args.length < 1) {
      System.out.println("Ошибка: укажите файл для загрузки данных");
      return;
    }

    if (!args[0].trim().endsWith(".xml")) {
      System.out.println("Ошибка: укажите файл с расширением .xml");
      return;
    }

    String filePath = args[0];
    XMLHandler xmlHandler = new XMLHandler(filePath);
    collection.setMovies(xmlHandler.loadLocal());

    Scanner scanner = new Scanner(System.in);
    CommandInvoker invoker = new CommandInvoker();

    // Регистрация команд
    invoker.register("help", new HelpCommand());
    invoker.register("info", new InfoCommand(collection));
    invoker.register("add", new AddCommand(collection, scanner, false));
    invoker.register("show", new ShowCommand(collection));
    invoker.register("clear", new ClearCommand(collection));
    invoker.register("exit", new ExitCommand());
    invoker.register("remove_first", new RemoveFirstProgram(collection));
    invoker.register("reorder", new ReorderCommand(collection));
    invoker.register("sum_of_length", new SumOfLengthCommand(collection));
    invoker.register("save", new SaveCommand(collection, filePath));
    invoker.register(
        "print_field_descending_oscars_count", new PrintDescendingOscarsCountCommand(collection));

    while (scanner.hasNextLine()) {
      String command = scanner.nextLine().trim();
      handleCommand(command, collection, scanner, invoker, false);
    }
  }
}
