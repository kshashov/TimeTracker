package com.github.kshashov.timetracker.web.ui.components;

import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.shared.Registration;
import lombok.Getter;

import java.util.function.Consumer;

@Getter
public class ConfirmDialog extends ButtonsDialog {

    private final H3 titleField = new H3();
    private final Button ok = UIUtils.createPrimaryButton("Ok");
    private final Button cancel = UIUtils.createTertiaryButton("Cancel");
    private final String title;
    private final Consumer<Boolean> callback;
    private Registration registrationForOk;
    private Label statusText = UIUtils.createErrorLabel("");

    public ConfirmDialog(String title, Consumer<Boolean> callback) {
        this.title = title;
        this.callback = callback;

        initContent();
        initButtonBar();
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
    }

    private void initContent() {
        initTitle();
    }

    private void initTitle() {
        getContent().add(titleField);
    }

    private void initButtonBar() {
        ok.setAutofocus(true);
        ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(ok, cancel);
    }


    public final void open() {
        titleField.setText(title);

        if (registrationForOk != null) {
            registrationForOk.remove();
        }

        registrationForOk = ok.addClickListener(e -> okClicked());
        cancel.addClickListener(e -> close());

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
