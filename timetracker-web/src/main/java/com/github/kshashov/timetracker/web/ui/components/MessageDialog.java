package com.github.kshashov.timetracker.web.ui.components;

import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.github.kshashov.timetracker.web.ui.util.css.TextAlign;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.shared.Registration;
import lombok.Getter;

@Getter
public class MessageDialog extends ButtonsDialog {

    private final String title;
    private final String description;
    private Registration registrationForOk;

    private final Span descriptionField = new Span();
    private final Label titleField = UIUtils.createH3Label("");
    private final Button ok = UIUtils.createPrimaryButton("Ok");

    public MessageDialog(String title, String description) {
        this.title = title;
        this.description = description;

        initContent();
        initButtonBar();
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
    }

    private void initContent() {
        initTitle();
        initDescription();
    }

    private void initTitle() {
        titleField.setText(title);
        getContent().add(titleField);
    }

    private void initDescription() {
        UIUtils.setTextAlign(TextAlign.CENTER, descriptionField);
        descriptionField.setText(description);
        getContent().setAlignItems(FlexComponent.Alignment.CENTER);
        getContent().add(descriptionField);
    }

    private void initButtonBar() {
        ok.setAutofocus(true);
        ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        getFooter().add(ok);
    }


    public final void open() {
        if (registrationForOk != null) {
            registrationForOk.remove();
        }

        registrationForOk = ok.addClickListener(e -> close());

        super.open();
    }

    @Override
    public void close() {
        super.close();
    }
}
