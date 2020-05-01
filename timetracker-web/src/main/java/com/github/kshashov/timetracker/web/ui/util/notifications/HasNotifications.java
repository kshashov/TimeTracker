package com.github.kshashov.timetracker.web.ui.util.notifications;

import lombok.AllArgsConstructor;
import lombok.Getter;

public interface HasNotifications extends HasEventBus {

    default void notify(String text) {
        fire(new NotificationEvent(NotificationType.MESSAGE, text));
    }

    default void notifyError(String text) {
        fire(new NotificationEvent(NotificationType.ERROR, text));
    }

    default void notifyPopup(String text) {
        fire(new NotificationEvent(NotificationType.POPUP, text));
    }

    @Getter
    @AllArgsConstructor
    class NotificationEvent {
        private final NotificationType type;
        private final String message;
    }

    enum NotificationType {
        MESSAGE, ERROR, POPUP
    }
}
