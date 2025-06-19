package common.dto;

import java.io.Serializable;
import java.util.Objects;


public class Request implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String commandName;
    private final String commandStringArgument;
    private final Serializable commandObjectArgument;
    private User user; // User credentials for authentication, sent with every request

    public Request(String commandName, String commandStringArgument, Serializable commandObjectArgument, User user) {
        this.commandName = commandName;
        this.commandStringArgument = commandStringArgument;
        this.commandObjectArgument = commandObjectArgument;
        this.user = user;
    }

    public Request(String commandName, String commandStringArgument, User user) {
        this(commandName, commandStringArgument, null, user);
    }

    public Request(String commandName, User user) {
        this(commandName, "", null, user);
    }

    public String getCommandName() { return commandName; }
    public String getStringArg() { return commandStringArgument; }
    public Serializable getObjectArg() { return commandObjectArgument; }
    public User getUser() { return user; }

    @Override
    public String toString() {
        return "Request{" +
                "commandName='" + commandName + '\'' +
                ", stringArg='" + commandStringArgument + '\'' +
                ", objectArg=" + (commandObjectArgument != null ? commandObjectArgument.getClass().getSimpleName() : "null") +
                ", user=" + (user != null ? user.getUsername() : "null") +
                '}';
    }
}