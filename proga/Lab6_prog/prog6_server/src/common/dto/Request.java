package common.dto;

import java.io.Serializable;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;
    private String commandName;
    private String commandStringArgument;
    private Serializable commandObjectArgument;

    public Request(String commandName, String commandStringArgument) {
        this.commandName = commandName;
        this.commandStringArgument = commandStringArgument;
        this.commandObjectArgument = null;
    }

    public Request(String commandName, String commandStringArgument, Serializable commandObjectArgument) {
        this.commandName = commandName;
        this.commandStringArgument = commandStringArgument;
        this.commandObjectArgument = commandObjectArgument;
    }

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