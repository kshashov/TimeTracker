package com.github.kshashov.timetracker.web.ui.components;

import com.github.kshashov.timetracker.web.ui.util.FontSize;
import com.github.kshashov.timetracker.web.ui.util.FontWeight;
import com.github.kshashov.timetracker.web.ui.util.LumoStyles;
import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.github.kshashov.timetracker.web.ui.util.css.BorderRadius;
import com.vaadin.flow.component.orderedlayout.FlexComponent;

public class Initials extends FlexBoxLayout {

    private String CLASS_NAME = "initials";

    public Initials(String initials) {
        setAlignItems(FlexComponent.Alignment.CENTER);
        setBackgroundColor(LumoStyles.Color.Contrast._10);
        setBorderRadius(BorderRadius.L);
        setClassName(CLASS_NAME);
        UIUtils.setFontSize(FontSize.S, this);
        UIUtils.setFontWeight(FontWeight._600, this);
        setHeight(LumoStyles.Size.M);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        setWidth(LumoStyles.Size.M);

        add(initials);
    }

}
