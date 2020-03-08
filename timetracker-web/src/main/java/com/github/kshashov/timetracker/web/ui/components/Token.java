package com.github.kshashov.timetracker.web.ui.components;

import com.github.kshashov.timetracker.web.ui.layout.size.Left;
import com.github.kshashov.timetracker.web.ui.layout.size.Right;
import com.github.kshashov.timetracker.web.ui.util.FontSize;
import com.github.kshashov.timetracker.web.ui.util.LumoStyles;
import com.github.kshashov.timetracker.web.ui.util.TextColor;
import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.github.kshashov.timetracker.web.ui.util.css.BorderRadius;
import com.github.kshashov.timetracker.web.ui.util.css.Display;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;

public class Token extends FlexBoxLayout {

    private final String CLASS_NAME = "token";

    public Token(String text) {
        setAlignItems(FlexComponent.Alignment.CENTER);
        setBackgroundColor(LumoStyles.Color.Primary._10);
        setBorderRadius(BorderRadius.M);
        setClassName(CLASS_NAME);
        setDisplay(Display.INLINE_FLEX);
        setPadding(Left.S, Right.XS);
        setSpacing(Right.XS);

        Label label = UIUtils.createLabel(FontSize.S, TextColor.BODY, text);
        Button button = UIUtils.createButton(VaadinIcon.CLOSE_SMALL, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY_INLINE);
        add(label, button);
    }

}
