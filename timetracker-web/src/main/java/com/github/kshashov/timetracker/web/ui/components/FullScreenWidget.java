package com.github.kshashov.timetracker.web.ui.components;

import com.github.kshashov.timetracker.web.ui.layout.size.Bottom;
import com.github.kshashov.timetracker.web.ui.layout.size.Uniform;
import com.github.kshashov.timetracker.web.ui.layout.size.Vertical;
import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.github.kshashov.timetracker.web.ui.util.css.BorderRadius;
import com.github.kshashov.timetracker.web.ui.util.css.FlexDirection;
import com.github.kshashov.timetracker.web.ui.util.css.Shadow;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;

public class FullScreenWidget extends FullScreenLayout {
    private FlexBoxLayout layout = new FlexBoxLayout();
    private FlexBoxLayout header = new FlexBoxLayout();
    private FlexBoxLayout content = new FlexBoxLayout();
    private FlexBoxLayout footer = new FlexBoxLayout();

    public FullScreenWidget() {
        super();

        initLayout();

        add(layout);
    }

    private void initLayout() {
        layout.setShadow(Shadow.XL);
        layout.setSpacing(Vertical.XS);
        layout.setFlexDirection(FlexDirection.COLUMN);
        layout.setBorderRadius(BorderRadius.L);

        initHeader(header);
        initContent(content);
        initFooter(footer);

        FlexBoxLayout internal = new FlexBoxLayout();
        internal.setFlexDirection(FlexDirection.COLUMN);
        internal.setPadding(Uniform.L);
        internal.setSpacing(Vertical.XS);
        internal.add(header, content);

        layout.add(internal);
        layout.add(footer);
    }

    protected void initContent(FlexBoxLayout content) {
        content.setAlignItems(Alignment.CENTER);
        content.setJustifyContentMode(JustifyContentMode.CENTER);
        content.setVisible(false);
    }

    protected void initHeader(FlexBoxLayout header) {
        header.setVisible(false);
    }

    protected void initFooter(FlexBoxLayout footer) {
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
        header.setMargin(Bottom.M);
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
        Label label = UIUtils.createH3Label(title);
        addHeaderItems(label);
    }
}
