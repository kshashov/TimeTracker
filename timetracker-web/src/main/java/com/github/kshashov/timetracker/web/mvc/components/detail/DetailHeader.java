package com.github.kshashov.timetracker.web.mvc.components.detail;

import com.github.kshashov.timetracker.web.mvc.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.mvc.layout.size.Horizontal;
import com.github.kshashov.timetracker.web.mvc.layout.size.Right;
import com.github.kshashov.timetracker.web.mvc.layout.size.Vertical;
import com.github.kshashov.timetracker.web.mvc.util.BoxShadowBorders;
import com.github.kshashov.timetracker.web.mvc.util.UIUtils;
import com.github.kshashov.timetracker.web.mvc.util.css.FlexDirection;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tabs;

public class DetailHeader extends FlexBoxLayout {

    private static final String CLASS_NAME = "detail-header";

    private Button close;
    private Label title;

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
        wrapper.setPadding(Horizontal.RESPONSIVE_L, Vertical.M);
        wrapper.setSpacing(Right.L);
        add(wrapper);
    }

    public DetailHeader(String title, Tabs tabs) {
        this(title);
        add(tabs);
    }

    public void setTitle(String title) {
        this.title.setText(title);
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
