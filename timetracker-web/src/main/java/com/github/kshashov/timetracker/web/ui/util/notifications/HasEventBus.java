package com.github.kshashov.timetracker.web.ui.util.notifications;

import com.google.common.eventbus.EventBus;

public interface HasEventBus {

    EventBus eventBus();

    default void fire(Object event) {
        eventBus().post(event);
    }
}
