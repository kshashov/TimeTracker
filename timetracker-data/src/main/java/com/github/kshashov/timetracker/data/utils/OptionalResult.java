package com.github.kshashov.timetracker.data.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OptionalResult<T> {

    private final T result;
    private final String message;

    public static <T> OptionalResult<T> Success(T result) {
        return new OptionalResult<>(result, null);
    }

    public static <T> OptionalResult<T> Fail(String message) {
        return new OptionalResult<>(null, message);
    }

    public boolean hasResult() {
        return result != null;
    }

    public boolean hasMessage() {
        return message != null;
    }
}
