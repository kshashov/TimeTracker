package com.github.kshashov.timetracker.web.ui.components;

import com.vaadin.flow.component.dependency.CssImport;

@CssImport("./styles/components/fullscreen.css")
public class FullScreenLayout extends FlexBoxLayout {
    private final static String CLASS_NAME = "fullscreen-layout";

    public FullScreenLayout() {
        super();

        setClassName(CLASS_NAME);
    }
}
