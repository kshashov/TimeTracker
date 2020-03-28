package com.github.kshashov.timetracker.web.ui.components;

import com.github.kshashov.timetracker.web.ui.layout.size.Vertical;
import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;

public class FullScreenWidget extends FullScreenLayout {
    private final static String CLASS_NAME = "fullscreen-widget";

    private final FlexBoxLayout layout = new FlexBoxLayout();
    private final FlexBoxLayout header = new FlexBoxLayout();
    private final Label title = UIUtils.createH3Label("");
    private final FlexBoxLayout actions = new FlexBoxLayout();
    private final FlexBoxLayout content = new FlexBoxLayout();
    private final FlexBoxLayout footer = new FlexBoxLayout();

    public FullScreenWidget() {
        super();

        initLayout();

        add(layout);
    }

    private void initLayout() {
        layout.addClassName(CLASS_NAME);
        layout.setSpacing(Vertical.XS);

        initHeader(header);
        initContent(content);
        initFooter(footer);

        FlexBoxLayout internal = new FlexBoxLayout();
        internal.addClassName(CLASS_NAME + "__content-footer");
        internal.setSpacing(Vertical.XS);
        internal.add(header, content);

        layout.add(internal);
        layout.add(footer);
    }

    protected void initContent(FlexBoxLayout content) {
        content.addClassName(CLASS_NAME + "__content");

        content.setVisible(false);
    }

    protected void initHeader(FlexBoxLayout header) {
        header.addClassName(CLASS_NAME + "__header");
        header.add(title);
        header.add(actions);

        header.setVisible(false);
    }

    protected void initFooter(FlexBoxLayout footer) {
        footer.addClassName(CLASS_NAME + "__footer");

        footer.setVisible(false);
    }

    protected FlexBoxLayout getHeader() {
        return header;
    }

    protected FlexBoxLayout getContent() {
        return content;
    }

    protected FlexBoxLayout getFooter() {
        return footer;
    }

    protected void addHeaderItems(Component... components) {
        header.add(components);
        header.setVisible(true);
    }

    protected void addContentItems(Component... components) {
        content.add(components);
        content.setVisible(true);
    }

    protected void addFooterItems(Component... components) {
        footer.add(components);
        footer.setVisible(true);
    }

    protected void setTitle(String title) {
        this.title.setText(title);
        header.setVisible(true);
    }
}
