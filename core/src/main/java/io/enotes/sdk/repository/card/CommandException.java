package io.enotes.sdk.repository.card;


import java.io.IOException;

/**
 * A Custom Command exception to wrap {@link IOException} or other exception.
 */
public class CommandException extends Exception {
    private int code;

    public CommandException(Throwable cause) {
        super(cause);
    }

//    public CommandException(String message) {
//        super(message);
//    }

    public CommandException(int code, String message) {
        super(message);
        this.code = code;
    }

    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }

    public int getCode() {
        return code;
    }
}
