package com.example.server;

import com.example.app.Command;
import com.example.app.CommandData;
import com.example.app.CommandFactory;
import com.example.app.CommandInvoker;
import com.example.app.commands.*;
import com.example.network.Response;
import com.example.service.MovieCollection;
import com.example.service.UserManager;
import com.example.service.model.Movie;
import com.example.service.model.User;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.*;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Server {
  private static final Logger logger = LogManager.getLogger(Server.class);
  private static final Dotenv dotenv = Dotenv.load();
  private final ForkJoinPool responsePool = ForkJoinPool.commonPool();
  private final MovieCollection collection;
  private final UserManager userManager;
  private final ConnectionHandler connectionHandler;
  private final RequestReader requestReader;
  private final ResponseSender responseSender;
  private final BufferedReader consoleReader;
  private final Map<String, CommandFactory> commandFactories;

  public Server(int port) throws IOException {
    this.collection = new MovieCollection();
    this.userManager = new UserManager();
    this.connectionHandler = new ConnectionHandler(dotenv.get("HOST"), port);
    this.requestReader = new RequestReader();
    this.responseSender = new ResponseSender();
    this.consoleReader = new BufferedReader(new InputStreamReader(System.in));
    this.commandFactories = new HashMap<>();
    registerCommandFactories();
  }

  private void registerCommandFactories() {
    commandFactories.put(
        "add",
        (collection, commandData, login, password) -> {
          if (commandData.arguments() instanceof Movie movie) {
            return new AddCommand(collection, movie, login, password);
          }
          throw new IllegalArgumentException(
              "Некорректные аргументы для команды add: ожидается Movie");
        });

    commandFactories.put(
        "update",
        (collection, commandData, login, password) -> {
          if (commandData.arguments() instanceof Object[] arr
              && arr.length == 2
              && arr[0] instanceof Movie movie
              && arr[1] instanceof String input) {
            return new UpdateCommand(collection, movie, input, login, password);
          }
          throw new IllegalArgumentException(
              "Некорректные аргументы для команды update: ожидается Movie и String");
        });

    commandFactories.put(
        "remove_by_id",
        (collection, commandData, login, password) -> {
          if (commandData.arguments() instanceof String input) {
            return new RemoveByIdCommand(collection, input, login, password);
          }
          throw new IllegalArgumentException(
              "Некорректные аргументы для команды remove_by_id: ожидается String");
        });

    commandFactories.put(
        "remove_at",
        (collection, commandData, login, password) -> {
          if (commandData.arguments() instanceof String input) {
            return new RemoveAtCommand(collection, input, login, password);
          }
          throw new IllegalArgumentException(
              "Некорректные аргументы для команды remove_at: ожидается String");
        });

    commandFactories.put(
        "count_by_operator",
        (collection, commandData, login, password) -> {
          if (commandData.arguments() instanceof String input) {
            return new CountByOperator(collection, input, login, password);
          }
          throw new IllegalArgumentException(
              "Некорректные аргументы для команды count_by_operator: ожидается String");
        });

    commandFactories.put(
        "help", (collection, commandData, login, password) -> new HelpCommand(login, password));

    commandFactories.put(
        "info",
        (collection, commandData, login, password) -> new InfoCommand(collection, login, password));

    commandFactories.put(
        "show",
        (collection, commandData, login, password) -> new ShowCommand(collection, login, password));

    commandFactories.put(
        "clear",
        (collection, commandData, login, password) ->
            new ClearCommand(collection, login, password));

    commandFactories.put(
        "remove_first",
        (collection, commandData, login, password) ->
            new RemoveFirstCommand(collection, login, password));

    commandFactories.put(
        "reorder",
        (collection, commandData, login, password) ->
            new ReorderCommand(collection, login, password));

    commandFactories.put(
        "sum_of_length",
        (collection, commandData, login, password) ->
            new SumOfLengthCommand(collection, login, password));

    commandFactories.put(
        "print_field_descending_oscars_count",
        (collection, commandData, login, password) ->
            new PrintDescendingOscarsCountCommand(collection, login, password));

    commandFactories.put(
        "register",
        new CommandFactory() {
          @Override
          public Command createCommand(
              MovieCollection collection, CommandData commandData, String login, String password) {
            return new RegisterCommand(login, password);
          }

          @Override
          public boolean requiresAuth() {
            return false;
          }
        });

    commandFactories.put(
        "login",
        new CommandFactory() {
          @Override
          public Command createCommand(
              MovieCollection collection, CommandData commandData, String login, String password) {
            return new LoginCommand(login, password);
          }

          @Override
          public boolean requiresAuth() {
            return false;
          }
        });
  }

  public void start() throws IOException {
    CommandInvoker invoker = new CommandInvoker();
    logger.info("Сервер запущен");

    while (true) {
      if (consoleReader.ready()) {
        String input = consoleReader.readLine().trim().toLowerCase();
        if (input.equals("exit")) {
          logger.info("Сервер останавливается и сохраняет коллекцию в файл");
          break;
        }
      }

      ReceiveResult result = connectionHandler.receive();
      if (result != null) {
        new Thread(() -> processRequest(result, invoker)).start();
      }
    }
  }

  private void processRequest(ReceiveResult result, CommandInvoker invoker) {
    try {
      CommandData commandData = requestReader.readCommandData(result.buffer().array());
      logger.debug("Команда дересериализована: {}", commandData.name());

      // проверка авторизации
      String login = commandData.login();
      String password = commandData.password();
      String commandName = commandData.name().toLowerCase();

      CommandFactory factory = commandFactories.get(commandName);
      if (factory == null) {
        Response response = new Response("Неизвестная команда: " + commandName, false);
        //        responsePool.execute(
        //            () -> {
        //              try {
        //                responseSender.sendResponse(
        //                    connectionHandler.getChannel(), result.clientAddress(), response);
        //              } catch (IOException e) {
        //                logger.error("Ошибка отправки ответа", e);
        //              }
        //            });
        sendResponseSafety(commandName, "неизвестная команда", response, result.clientAddress());
        return;
      }

      if (factory.requiresAuth()) {
        User user = userManager.verify(login, password);
        if (user == null) {
          Response response = new Response("Неавторизованный доступ", false);
          sendResponseSafety(
              commandName, "неавторизованный доступ", response, result.clientAddress());
          //          responsePool.execute(
          //              () -> {
          //                try {
          //                  responseSender.sendResponse(
          //                      connectionHandler.getChannel(),
          //                      result.clientAddress(),
          //                      new Response("Неавторизованный доступ", false));
          //                } catch (IOException e) {
          //                  logger.error("Ошибка отправки ответа", e);
          //                }
          //              });
          return;
        }
      }

      Response response;
      try {
        Command command = factory.createCommand(collection, commandData, login, password);
        response = invoker.execute(command, userManager);
      } catch (IllegalArgumentException e) {
        logger.warn("Некорректные аргументы для команды {}: {}", commandName, e.getMessage(), e);
        response = new Response(e.getMessage(), false);
      } catch (Exception e) {
        logger.error("Ошибка обработки команды {}: {}", commandName, e.getMessage(), e);
        response = new Response("Ошибка сервера: " + e.getMessage(), false);
      }
      sendResponseSafety(commandName, "команда", response, result.clientAddress());

      // обработка команды в отдельном потоке
      //      new Thread(
      //              () -> {
      //                Response response;
      //                try {
      //                  Command command = factory.createCommand(collection, commandData, login,
      // password);
      //                  response = invoker.execute(command, userManager);
      //                } catch (IllegalArgumentException e) {
      //                  logger.warn(
      //                      "Некорректные аргументы для команды {}: {}", commandName,
      // e.getMessage());
      //                  response = new Response(e.getMessage(), false);
      //                } catch (Exception e) {
      //                  logger.error("Ошибка обработки команды {}: {}", commandName,
      // e.getMessage(), e);
      //                  response = new Response("Ошибка сервера: " + e.getMessage(), false);
      //                }
      //                Response finalResponse = response;
      //                responsePool.execute(
      //                    () -> {
      //                      try {
      //                        responseSender.sendResponse(
      //                            connectionHandler.getChannel(), result.clientAddress(),
      // finalResponse);
      //                      } catch (IOException e) {
      //                        throw new RuntimeException(e);
      //                      }
      //                    });
      //              })
      //          .start();
    } catch (IOException e) {
      logger.error("Ошибка обработки запроса: {}", e.getMessage(), e);
      Response response = new Response("Ошибка сервера: " + e.getMessage(), false);
      sendResponseSafety("unknown", "обработка запроса", response, result.clientAddress());
      //      responsePool.execute(
      //          () -> {
      //            try {
      //              responseSender.sendResponse(
      //                  connectionHandler.getChannel(),
      //                  result.clientAddress(),
      //                  new Response("Ошибка сервера: " + e.getMessage(), false));
      //            } catch (IOException ex) {
      //              logger.error("Ошибка отправки ответа", ex);
      //            }
      //          });
    }
  }

  private void sendResponseSafety(
      String commandName, String context, Response response, SocketAddress clientAddress) {
    try {
      responseSender.sendResponse(connectionHandler.getChannel(), clientAddress, response);
    } catch (IOException e) {
      logger.error("Ошибка отправки ответа для {} {}: {}", context, commandName, e.getMessage(), e);
    }
  }

  public static void main(String[] args) {
    try {
      Server server = new Server(Integer.parseInt(dotenv.get("PORT")));
      server.start();
    } catch (IOException e) {
      logger.error("Ошибка работы сервера", e);
    }
  }
}
