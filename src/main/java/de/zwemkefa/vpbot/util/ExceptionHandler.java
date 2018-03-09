package de.zwemkefa.vpbot.util;

@FunctionalInterface
public interface ExceptionHandler {
    void handleException(Exception e);
}
