package com.github.kshashov.timetracker.web.mvc.components;

import com.github.kshashov.timetracker.web.mvc.util.UIUtils;
import com.github.kshashov.timetracker.web.mvc.util.css.lumo.BadgeColor;
import com.github.kshashov.timetracker.web.mvc.util.css.lumo.BadgeShape;
import com.github.kshashov.timetracker.web.mvc.util.css.lumo.BadgeSize;
import com.vaadin.flow.component.html.Span;

import java.util.StringJoiner;

import static com.github.kshashov.timetracker.web.mvc.util.css.lumo.BadgeShape.PILL;

public class Badge extends Span {

    public Badge(String text) {
        this(text, BadgeColor.NORMAL);
    }

    public Badge(String text, BadgeColor color) {
        super(text);
        UIUtils.setTheme(color.getThemeName(), this);
    }

    public Badge(String text, BadgeColor color, BadgeSize size, BadgeShape shape) {
        super(text);
        StringJoiner joiner = new StringJoiner(" ");
        joiner.add(color.getThemeName());
        if (shape.equals(PILL)) {
            joiner.add(shape.getThemeName());
        }
        if (size.equals(BadgeSize.S)) {
            joiner.add(size.getThemeName());
        }
        UIUtils.setTheme(joiner.toString(), this);
    }

}
