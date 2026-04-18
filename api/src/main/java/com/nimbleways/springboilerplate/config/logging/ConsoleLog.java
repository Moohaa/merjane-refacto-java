package com.nimbleways.springboilerplate.config.logging;

public class ConsoleLog implements Log {
    private final Class<?> clazz;

    ConsoleLog(Class<?> clazz) {
        this.clazz = clazz;
    }
    @Override
    public void info(String message) {
       System.out.println(String.format("[%s] %s", clazz.getName(), message));
    }
    @Override
    public void warn(String message) {
        System.out.println(String.format("[%s] %s", clazz.getName(), message));
    }

    @Override
    public void error(String message) {
        System.out.println(String.format("[%s] %s", clazz.getName(), message));
    }
}
