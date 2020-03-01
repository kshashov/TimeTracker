package com.github.kshashov.timetracker.core.errors;

public class IncorrectArgumentException extends TimeTrackerException {
    public IncorrectArgumentException() {
    }

    public IncorrectArgumentException(String message) {
        super(message);
    }

    public IncorrectArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncorrectArgumentException(Throwable cause) {
        super(cause);
    }

    public IncorrectArgumentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
