package de.zwemkefa.vpbot.util;

@FunctionalInterface
public interface ExceptionHandler {
    public void handleException(Exception e);
}
