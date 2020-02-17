package com.github.kshashov.timetracker.web.mvc.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;

@CssImport(value = "frontend://styles/components/no-padding-dialog.css", themeFor = "vaadin-dialog-overlay")
public class NoPaddingDialog extends Dialog {

    public NoPaddingDialog() {
        getElement().setAttribute("theme", "no-padding-dialog");
    }

    public NoPaddingDialog(Component... components) {
        super(components);
        getElement().setAttribute("theme", "no-padding-dialog");
    }
}
