package com.example.app;

import java.io.Serializable;

/**
 * Класс для передачи данных команды с клиента на сервер. Содержит название команды и ее аргументы
 */
public record CommandData(String name, Object arguments, String login, String password)
    implements Serializable {}
