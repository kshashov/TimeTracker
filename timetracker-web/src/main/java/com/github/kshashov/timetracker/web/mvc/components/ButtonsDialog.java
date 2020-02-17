package com.github.kshashov.timetracker.web.mvc.components;

import com.github.kshashov.timetracker.web.mvc.layout.size.Horizontal;
import com.github.kshashov.timetracker.web.mvc.layout.size.Right;
import com.github.kshashov.timetracker.web.mvc.layout.size.Vertical;
import com.github.kshashov.timetracker.web.mvc.util.LumoStyles;
import com.github.kshashov.timetracker.web.mvc.util.css.BoxSizing;
import com.github.kshashov.timetracker.web.mvc.util.css.FlexDirection;
import com.vaadin.flow.component.Component;
import lombok.Getter;

@Getter
public class ButtonsDialog extends NoPaddingDialog {
    private final FlexBoxLayout content = new FlexBoxLayout();
    private final FlexBoxLayout footer = new FlexBoxLayout();

    public ButtonsDialog() {
        initContent();
        initFooter();
    }

    public ButtonsDialog(Component... components) {
        super(components);
        initContent();
        initFooter();
    }

    private void initContent() {
        content.setFlexDirection(FlexDirection.COLUMN);
        content.setPadding(Horizontal.L, Vertical.L);
        add(content);
    }

    private void initFooter() {
        footer.setBackgroundColor(LumoStyles.Color.Contrast._5);
        footer.setBoxSizing(BoxSizing.BORDER_BOX);
        footer.setPadding(Horizontal.RESPONSIVE_L, Vertical.S);
        footer.setSpacing(Right.S);
        footer.setWidthFull();
        add(footer);
    }
}
