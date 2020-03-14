package com.github.kshashov.timetracker.web.ui.util;

import com.github.kshashov.timetracker.core.errors.TimeTrackerException;
import com.vaadin.flow.data.binder.ValidationResult;
import org.apache.logging.log4j.util.Strings;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface DataHandler {

    default <T> ValidationResult handleDataManipulation(Supplier<T> dataManipulation) {
        return handleDataManipulation(dataManipulation, (result) -> {
        }, () -> {
        });
    }

    default <T> ValidationResult handleDataManipulation(Supplier<T> dataManipulation, Consumer<T> onSuccess) {
        return handleDataManipulation(dataManipulation, onSuccess, () -> {
        });
    }

    default <T> ValidationResult handleDataManipulation(Supplier<T> dataManipulation, Consumer<T> onSuccess, Callback onFail) {
        boolean isSuccess = false;
        T result = null;
        try {
            result = dataManipulation.get();
            isSuccess = true;
        } catch (TimeTrackerException ex) {
            if (!Strings.isBlank(ex.getMessage())) {
                UIUtils.showErrorNotification(ex.getMessage());
                return ValidationResult.error(ex.getMessage());
            } else {
                UIUtils.showErrorNotification("Unexpected server error. Please try again later");
            }
        } catch (Exception ex) {
            UIUtils.showErrorNotification("Unexpected server error. Please try again later");
        }

        if (isSuccess) {
            onSuccess.accept(result);
        } else {
            onFail.execute();
        }

        return ValidationResult.ok();
    }

    @FunctionalInterface
    interface Callback {
        void execute();
    }
}
