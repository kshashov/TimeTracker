package com.github.kshashov.timetracker.web.ui.components;

import com.github.kshashov.timetracker.web.ui.layout.size.Right;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;

@CssImport("./styles/components/fullscreen-buttons.css")
public class FullScreenButtonsWidget extends FullScreenWidget {

    public void addActions(Component... components) {
        getFooter().add(components);
    }

    @Override
    protected void initFooter(FlexBoxLayout footer) {
        super.initFooter(footer);

        footer.setSpacing(Right.S);
    }
}
