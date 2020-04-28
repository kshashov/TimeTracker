package com.github.kshashov.timetracker.web.ui.views.entries.actionchooser;

import com.github.kshashov.timetracker.data.entity.Action;
import com.vaadin.flow.component.HasValue;

public class ActionChangeEvent implements HasValue.ValueChangeEvent<Action> {
    @Override
    public HasValue<?, Action> getHasValue() {
        return null;
    }

    @Override
    public boolean isFromClient() {
        return false;
    }

    @Override
    public Action getOldValue() {
        return null;
    }

    @Override
    public Action getValue() {
        return null;
    }
}
