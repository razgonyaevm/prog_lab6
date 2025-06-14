package com.example.app.commands;

import com.example.app.Command;
import com.example.network.Response;
import com.example.service.UserManager;
import com.example.service.model.User;

/** Вывод справки */
public class HelpCommand implements Command {
  private final String login;
  private final String password;

  public HelpCommand(String login, String password) {
    this.login = login;
    this.password = password;
  }

  @Override
  public Response execute(UserManager userManager) {
    User user = userManager.verify(login, password);
    if (user == null) {
      return new Response("Неавторизованный доступ", false);
    }
    return new Response(
        "Доступные команды:\n\nhelp : вывести справку по доступным командам\n\ninfo : вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)\n\nshow : вывести в стандартный поток вывода все элементы коллекции в строковом представлении\n\nadd {element} : добавить новый элемент в коллекцию\n\nupdate id {element} : обновить значение элемента коллекции, id которого равен заданному\n\nremove_by_id id : удалить элемент из коллекции по его id\n\nclear : очистить коллекцию\n\nexecute_script file_name : считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме. Команды выполняются с исходной коллекцией\n\nexit : завершить программу (без сохранения в файл)\n\nremove_at index : удалить элемент, находящийся в заданной позиции коллекции (index)\n\nremove_first : удалить первый элемент из коллекции\n\nreorder : отсортировать коллекцию в порядке, обратном нынешнему\n\nsum_of_length : вывести сумму значений поля length для всех элементов коллекции\n\ncount_by_operator operator : вывести количество элементов, значение поля operator которых равно заданному (вводится имя оператора)\n\nprint_field_descending_oscars_count : вывести значения поля oscarsCount всех элементов в порядке убывания",
        true);
  }
}
