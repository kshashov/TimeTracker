package com.github.kshashov.timetracker.web.ui.components.detail;

import com.github.kshashov.timetracker.web.ui.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.ui.layout.size.Horizontal;
import com.github.kshashov.timetracker.web.ui.layout.size.Right;
import com.github.kshashov.timetracker.web.ui.layout.size.Vertical;
import com.github.kshashov.timetracker.web.ui.util.BoxShadowBorders;
import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.github.kshashov.timetracker.web.ui.util.css.FlexDirection;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tabs;

public class DetailHeader extends FlexBoxLayout {

    private static final String CLASS_NAME = "detail-header";

    private Button close;
    private Label title;
    private final FlexBoxLayout actions = new FlexBoxLayout();

    public DetailHeader(String title) {
        setClassName(CLASS_NAME);
        addClassName(BoxShadowBorders.BOTTOM);
        setFlexDirection(FlexDirection.COLUMN);
        setWidthFull();

        this.close = UIUtils.createTertiaryInlineButton(VaadinIcon.CLOSE);
        this.close.setClassName(CLASS_NAME + "__close");
        UIUtils.setLineHeight("1", this.close);
        setCanReset(true);

        this.title = UIUtils.createH4Label(title);

        FlexBoxLayout wrapper = new FlexBoxLayout(this.close, this.title);
        wrapper.setAlignItems(Alignment.CENTER);
        wrapper.setSpacing(Right.L);

        FlexBoxLayout wrapper2 = new FlexBoxLayout(wrapper, actions);
        wrapper2.setJustifyContentMode(JustifyContentMode.BETWEEN);
        wrapper2.setPadding(Horizontal.RESPONSIVE_L, Vertical.XS);
        wrapper2.setAlignItems(Alignment.CENTER);
        add(wrapper2);
    }

    public DetailHeader(String title, Tabs tabs) {
        this(title);
        add(tabs);
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public void addActions(Component... components) {
        actions.add(components);
        actions.setVisible(true);
    }

    public FlexBoxLayout getActions() {
        return actions;
    }

    public void setCanReset(boolean isClosable) {
        if (isClosable) {
            close.getElement().setAttribute("reset", true);
        } else {
            close.getElement().removeAttribute("reset");
        }
    }

    public void addCloseListener(ComponentEventListener<ClickEvent<Button>> listener) {
        this.close.addClickListener(listener);
    }

}
