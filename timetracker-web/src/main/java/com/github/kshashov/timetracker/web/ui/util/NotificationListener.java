package com.github.kshashov.timetracker.web.ui.util;

import com.github.kshashov.timetracker.web.ui.components.MessageDialog;
import com.github.kshashov.timetracker.web.ui.util.notifications.HasNotifications;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class NotificationListener {
    private final EventBus eventBus;

    @Autowired
    public NotificationListener(EventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.register(this);
    }

    @Subscribe
    public void onNotificationEvent(HasNotifications.NotificationEvent event) {
        if (event.getType().equals(HasNotifications.NotificationType.MESSAGE)) {
            UIUtils.showNotification(StringUtils.defaultString(event.getMessage()));
        } else if (event.getType().equals(HasNotifications.NotificationType.ERROR)) {
            UIUtils.showErrorNotification(StringUtils.defaultString(event.getMessage()));
        } else if (event.getType().equals(HasNotifications.NotificationType.POPUP)) {
            new MessageDialog("", event.getMessage()).open();
        }
    }

    @PreDestroy
    private void onDestrioy() {
        eventBus.unregister(this);
    }
}
