package com.github.kshashov.timetracker.web.ui.util;

import rx.Subscription;

import java.util.List;

public interface HasSubscriptions {

    List<Subscription> getSubscriptions();

    default void subscribe(Subscription subscription) {
        getSubscriptions().add(subscription);
    }

    default void unsubscribeAll() {
        getSubscriptions().forEach(Subscription::unsubscribe);
        getSubscriptions().clear();
    }
}
