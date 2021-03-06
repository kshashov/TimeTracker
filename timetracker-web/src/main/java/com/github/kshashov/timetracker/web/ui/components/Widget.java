package com.github.kshashov.timetracker.web.ui.components;

import com.github.kshashov.timetracker.web.ui.layout.size.Horizontal;
import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.github.kshashov.timetracker.web.ui.util.css.FlexDirection;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;

@CssImport("./styles/components/widget.css")
public class Widget extends FlexBoxLayout {
    private static final String CLASS_NAME = "widget";

    private final Label title = UIUtils.createH4Label("");
    private final FlexBoxLayout header = new FlexBoxLayout();
    private final FlexBoxLayout content = new FlexBoxLayout();
    private final FlexBoxLayout actions = new FlexBoxLayout();

    public Widget() {
        super();

        initLayout();
    }

    public Widget(Component... components) {
        this();

        addContentItems(components);
    }

    protected void initLayout() {
        setClassName(CLASS_NAME);


        initHeader(header);
        initContent(content);

        add(header, content);
    }

    protected void initHeader(FlexBoxLayout header) {
        actions.setClassName(CLASS_NAME + "__actions");
        actions.setSpacing(Horizontal.XS);
        actions.getStyle().set("color", "#9386e9");
        title.getStyle().set("color", "#9386e9");

        header.setClassName(CLASS_NAME + "__header");

        header.add(title);
        header.add(actions);

        header.setVisible(false);
    }

    protected void initContent(FlexBoxLayout content) {
        content.setClassName(CLASS_NAME + "__content");
        content.setFlexDirection(FlexDirection.COLUMN);

        content.setVisible(false);
    }

    public FlexBoxLayout getHeader() {
        return header;
    }

    public FlexBoxLayout getContent() {
        return content;
    }

    public FlexBoxLayout getActions() {
        return actions;
    }

    public void addHeaderItems(Component... components) {
        header.add(components);
        header.setVisible(true);
    }

    public void addContentItems(Component... components) {
        content.add(components);
        content.setVisible(true);
    }

    public void setTitle(String title) {
        this.title.setText(title);
        header.setVisible(true);
    }

    public void addActions(Component... components) {
        actions.add(components);
        header.setVisible(true);
    }
}
