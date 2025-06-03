package com.example.server;

import com.example.app.Command;
import com.example.app.CommandData;
import com.example.app.CommandInvoker;
import com.example.app.commands.*;
import com.example.network.Response;
import com.example.service.MovieCollection;
import com.example.service.model.Movie;
import com.example.xml.XMLHandler;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Server {
  private static final Logger logger = LogManager.getLogger(Server.class);
  private static final Dotenv dotenv = Dotenv.load();
  private final MovieCollection collection;
  private final ConnectionHandler connectionHandler;
  private final RequestReader requestReader;
  private final ResponseSender responseSender;
  private final CommandInvoker invoker;
  private final BufferedReader consoleReader;

  public Server(String filePath, int port) throws IOException {
    this.collection = new MovieCollection();
    XMLHandler xmlHandler = new XMLHandler(filePath);
    this.collection.setMovies(xmlHandler.load());
    this.connectionHandler = new ConnectionHandler(dotenv.get("HOST"), port);
    this.requestReader = new RequestReader();
    this.responseSender = new ResponseSender();
    this.invoker = new CommandInvoker();
    this.consoleReader = new BufferedReader(new InputStreamReader(System.in));
  }

  public void start() throws IOException, InterruptedException {

    invoker.register("help", new HelpCommand());
    invoker.register("info", new InfoCommand(collection));
    invoker.register("show", new ShowCommand(collection));
    invoker.register("clear", new ClearCommand(collection));
    invoker.register("remove_first", new RemoveFirstProgram(collection));
    invoker.register("reorder", new ReorderCommand(collection));
    invoker.register("sum_of_length", new SumOfLengthCommand(collection));
    invoker.register(
        "print_field_descending_oscars_count", new PrintDescendingOscarsCountCommand(collection));

    logger.info("Сервер запущен");

    while (true) {
      if (consoleReader.ready()) {
        String input = consoleReader.readLine().trim().toLowerCase();
        if (input.equals("exit")) {
          logger.info("Сервер останавливается и сохраняет коллекцию в файл");
          saveCollection();
          break;
        }
      }
      ReceiveResult result = connectionHandler.receive();
      if (result != null) {
        try {
          CommandData commandData = requestReader.readCommandData(result.buffer().array());
          logger.debug("Команда дересериализована: {}", commandData.name());
          Response response = processCommandData(commandData);
          responseSender.sendResponse(
              connectionHandler.getChannel(), result.clientAddress(), response);
        } catch (IOException e) {
          logger.error("Ошибка обработки запроса: {}", e.getMessage(), e);
          responseSender.sendResponse(
              connectionHandler.getChannel(),
              result.clientAddress(),
              new Response("Ошибка сервера: " + e.getMessage(), false));
        }
      }
    }
  }

  private Response processCommandData(CommandData commandData) {
    String commandName = commandData.name().toLowerCase();
    Object args = commandData.arguments();

    Command command;
    switch (commandName) {
      case "add":
        if (args instanceof Movie movie) {
          command = new AddCommand(collection, movie);
        } else {
          return new Response("Некорректные аргументы для команды add: ожидается Movie", false);
        }
        break;
      case "update":
        if (args instanceof Object[] arr
            && arr.length == 2
            && arr[0] instanceof Movie movie
            && arr[1] instanceof String input) {
          command = new UpdateCommand(collection, movie, input);
        } else {
          return new Response(
              "Некорректные аргументы для команды update: ожидается Movie и строка", false);
        }
        break;
      default:
        if (invoker.isRegistered(commandName)) {
          command = invoker.getCommand(commandName);
        } else {
          return new Response("Неизвестная команда: " + commandName, false);
        }
        break;
    }

    return invoker.execute(command);
  }

  public void saveCollection() {
    logger.info(collection.save(dotenv.get("COLLECTION_FILE_PATH")));
  }

  public static void main(String[] args) {
    try {
      Server server =
          new Server(dotenv.get("COLLECTION_FILE_PATH"), Integer.parseInt(dotenv.get("PORT")));
      server.start();
    } catch (IOException | InterruptedException e) {
      logger.error("Ошибка работы сервера", e);
    }
  }
}
