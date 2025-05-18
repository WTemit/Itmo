package common.dto;

import java.io.Serializable;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L; // Good practice for Serializable
    private String commandName;
    private String commandStringArgument;
    private Serializable commandObjectArgument; // Can hold Worker, etc.

    // Constructor for commands without object args
    public Request(String commandName, String commandStringArgument) {
        this.commandName = commandName;
        this.commandStringArgument = commandStringArgument;
        this.commandObjectArgument = null;
    }

    // Constructor for commands with object args (e.g., add)
    public Request(String commandName, String commandStringArgument, Serializable commandObjectArgument) {
        this.commandName = commandName;
        this.commandStringArgument = commandStringArgument;
        this.commandObjectArgument = commandObjectArgument;
    }

    // Constructor for commands with only name (e.g., help, show)
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