package common.dto;

import java.io.Serializable;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;
    private String commandName;
    private String commandStringArgument;
    private Serializable commandObjectArgument; // Может содержать Worker и т.д.

    // Конструктор для команд без объектных аргументов
    public Request(String commandName, String commandStringArgument) {
        this.commandName = commandName;
        this.commandStringArgument = commandStringArgument;
        this.commandObjectArgument = null;
    }

    // Конструктор для команд с объектными аргументами (например, add)
    public Request(String commandName, String commandStringArgument, Serializable commandObjectArgument) {
        this.commandName = commandName;
        this.commandStringArgument = commandStringArgument;
        this.commandObjectArgument = commandObjectArgument;
    }

    // Конструктор для команд только с именем (например, help, show)
    public Request(String commandName) {
        this(commandName, "", null);
    }

    public String getCommandName() { return commandName; }
    public String getStringArg() { return commandStringArgument; }
    public Serializable getObjectArg() { return commandObjectArgument; }

    @Override
    public String toString() {
        return "Request{" + "commandName='" + commandName + '\'' + ", stringArg='" + commandStringArgument + '\'' + ", objectArg=" + (commandObjectArgument != null ? commandObjectArgument.getClass().getSimpleName() : "null") + '}';
    }
}