package com.example.server;

import static com.example.app.CommandProcessor.processCommand;

import com.example.app.Command;
import com.example.app.CommandInvoker;
import com.example.app.commands.*;
import com.example.network.Response;
import com.example.service.MovieCollection;
import com.example.xml.XMLHandler;
import java.io.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Server {
  private static final Logger logger = LogManager.getLogger(Server.class);
  private final MovieCollection collection;
  private final ConnectionHandler connectionHandler;
  private final RequestReader requestReader;
  private final ResponseSender responseSender;
  private final CommandInvoker invoker;

  public Server(String filePath, int port) throws IOException {
    this.collection = new MovieCollection();
    XMLHandler xmlHandler = new XMLHandler(filePath);
    this.collection.setMovies(xmlHandler.load().collection());
    this.connectionHandler = new ConnectionHandler(System.getenv("HOST"), port);
    this.requestReader = new RequestReader();
    this.responseSender = new ResponseSender();
    this.invoker = new CommandInvoker();
  }

  public void start() throws IOException, InterruptedException {

    invoker.register("help", new HelpCommand());
    invoker.register("info", new InfoCommand(collection));
    invoker.register("show", new ShowCommand(collection));
    invoker.register("clear", new ClearCommand(collection));
    invoker.register("exit", new ExitCommand());
    invoker.register("remove_first", new RemoveFirstProgram(collection));
    invoker.register("reorder", new ReorderCommand(collection));
    invoker.register("sum_of_length", new SumOfLengthCommand(collection));
    invoker.register(
        "print_field_descending_oscars_count", new PrintDescendingOscarsCountCommand(collection));

    logger.info("Сервер запущен");

    Runtime.getRuntime()
        .addShutdownHook(new Thread(this::saveCollection)); // Сохранение при завершении

    while (true) {
      ReceiveResult result = connectionHandler.receive();
      if (result != null) {
        try {
          Command command = requestReader.readCommand(result.buffer().array());
          logger.debug("Команда дересериализована: {}", command);
          Response response;
          if (command instanceof AddCommand addCommand) {
            response = invoker.execute(new AddCommand(collection, addCommand.getMovie()));
          } else if (command instanceof UpdateCommand updateCommand) {
            response =
                invoker.execute(
                    new UpdateCommand(
                        collection, updateCommand.getMovie(), updateCommand.getCommand()));
          } else {
            response = processCommand(command.toString(), collection, null, invoker, false);
          }
          responseSender.sendResponse(
              connectionHandler.getChannel(), result.clientAddress(), response);
        } catch (IOException e) {
          logger.error("Ошибка обработки запроса: {}", e.getMessage(), e);
          responseSender.sendResponse(
              connectionHandler.getChannel(),
              result.clientAddress(),
              new Response("Ошибка сервера: " + e.getMessage(), false));
        }
        Thread.sleep(100);
      }
    }
  }

  public void saveCollection() {
    collection.save("resources/collection.xml");
    logger.info("Коллекция сохранена в файл");
  }

  public static void main(String[] args) {
    try {
      Server server = new Server("resources/collection.xml", Integer.parseInt(System.getenv("PORT")));
      server.start();
    } catch (IOException | InterruptedException e) {
      logger.error("Ошибка работы сервера", e);
    }
  }
}
