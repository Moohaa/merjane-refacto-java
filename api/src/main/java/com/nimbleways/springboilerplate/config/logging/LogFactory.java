package com.nimbleways.springboilerplate.config.logging;

public class LogFactory {

    public static Log get(Class<?> clazz) {

        return new ConsoleLog(clazz);
    }
}
