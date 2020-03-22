package com.github.kshashov.timetracker.web.ui.components;

import com.github.kshashov.timetracker.web.ui.util.LumoStyles;

public class FullScreenLayout extends FlexBoxLayout {

    public FullScreenLayout() {
        super();

        setSizeFull();
        setClassName(LumoStyles.Color.Contrast._5);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }
}
