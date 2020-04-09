package com.github.kshashov.timetracker.web.ui.components;

import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.shared.Registration;
import lombok.Getter;

import java.util.function.Consumer;

@Getter
public class ConfirmDialog extends ButtonsDialog {

    private final String title;
    private final String description;
    private Consumer<Boolean> callback;
    private Registration registrationForOk;

    private final Label titleField = UIUtils.createH3Label("");
    private final Span descriptionField = new Span();
    private final Button ok = UIUtils.createPrimaryButton("Ok");
    private final Button cancel = UIUtils.createTertiaryButton("Cancel");
    private Label statusText = UIUtils.createErrorLabel("");

    public ConfirmDialog(String title, String description) {
        this.title = title;
        this.description = description;

        initContent();
        initButtonBar();
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
    }

    public ConfirmDialog(String title, String description, Consumer<Boolean> callback) {
        this(title, description);
        this.callback = callback;
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
        descriptionField.setText(description);
        getContent().add(descriptionField);
    }

    private void initButtonBar() {
        ok.setAutofocus(true);
        ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(ok, cancel);
    }


    public final void open() {
        if (registrationForOk != null) {
            registrationForOk.remove();
        }

        registrationForOk = ok.addClickListener(e -> okClicked());
        cancel.addClickListener(e -> close());

        super.open();
    }

    public final void open(Consumer<Boolean> callback) {
        this.callback = callback;
        open();
    }

    private void okClicked() {
        callback.accept(true);
        close();
    }

    @Override
    public void close() {
        super.close();
    }
}
