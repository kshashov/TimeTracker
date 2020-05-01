package com.github.kshashov.timetracker.web.ui.components.navigation.drawer;

import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;

@CssImport("frontend://styles/components/brand-expression.css")
public class BrandExpression extends Div {

    private String CLASS_NAME = "brand-expression";

    private Label title;

    public BrandExpression(String text) {
        setClassName(CLASS_NAME);

        title = UIUtils.createH3Label(text);

        add(title);
    }

}
