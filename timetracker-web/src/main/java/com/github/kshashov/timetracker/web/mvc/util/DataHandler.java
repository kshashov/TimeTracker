package com.github.kshashov.timetracker.web.mvc.util;

import com.github.kshashov.timetracker.data.utils.OptionalResult;

import java.util.function.Consumer;

public interface DataHandler {

    default <T> boolean handleDataManipulation(OptionalResult<T> dataManipulationResult) {
        return handleDataManipulation(dataManipulationResult, (result) -> {
        }, () -> {
        });
    }

    default <T> boolean handleDataManipulation(OptionalResult<T> dataManipulationResult, Consumer<T> onSuccess) {
        return handleDataManipulation(dataManipulationResult, onSuccess, () -> {
        });
    }

    default <T> boolean handleDataManipulation(OptionalResult<T> dataManipulationResult, Consumer<T> onSuccess, Callback onFailure) {
        if (dataManipulationResult.hasResult()) {
            if (dataManipulationResult.hasMessage()) {
                UIUtils.showSuccessNotification(dataManipulationResult.getMessage());
            }
            onSuccess.accept(dataManipulationResult.getResult());
        } else {
            if (dataManipulationResult.hasMessage()) {
                UIUtils.showErrorNotification(dataManipulationResult.getMessage());
            }
            onFailure.execute();
        }
        return dataManipulationResult.hasResult();
    }

    @FunctionalInterface
    interface Callback {
        void execute();
    }
}
