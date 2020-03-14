package com.github.kshashov.timetracker.web.ui.components;

import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.shared.Registration;

import java.util.function.Function;

public abstract class AbstractEditorDialog<T> extends ButtonsDialog {

    private final H3 titleField = new H3();
    private final FormLayout formLayout = new FormLayout();
    private final Button save = UIUtils.createPrimaryButton("Save");
    private final Button cancel = UIUtils.createTertiaryButton("Cancel");
    private final String title;
    private Function<? super T, ValidationResult> validator;
    private Registration registrationForSave;
    private Binder<T> binder = new Binder<>();
    private T currentItem;
    private Label statusText = UIUtils.createErrorLabel("");

    protected AbstractEditorDialog(String title) {
        this(title, param -> ValidationResult.ok());
    }

    protected AbstractEditorDialog(String title, Function<? super T, ValidationResult> itemSaver) {
        this.title = title;
        this.validator = itemSaver;
        binder.withValidator((value, context) -> validator.apply(value));

        initContent();
        initButtonBar();
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
    }

    private void initContent() {
        initTitle();
        initFormLayout();
    }

    private void initTitle() {
        getContent().add(titleField);
        getContent().add(statusText);
    }

    private void initFormLayout() {
        binder.setStatusLabel(statusText);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("25em", 2));
        Div div = new Div(formLayout);
        div.addClassName("has-padding");
        getContent().add(div);
    }

    private void initButtonBar() {
        save.setAutofocus(true);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancel.addClickListener(e -> close());
        getFooter().add(save, cancel);
    }

    protected final FormLayout getFormLayout() {
        return formLayout;
    }

    protected final Binder<T> getBinder() {
        return binder;
    }

    protected final T getCurrentItem() {
        return currentItem;
    }

    public final void open(T item) {
        open(item, this.validator);
    }

    public final void open(T item, Function<? super T, ValidationResult> validator) {
        this.validator = validator;

        currentItem = item;
        titleField.setText(title);
        if (registrationForSave != null) {
            registrationForSave.remove();
        }
        registrationForSave = save.addClickListener(e -> saveClicked());
        onDialogOpened(item);
        binder.readBean(currentItem);

        open();
    }

    protected abstract void onDialogOpened(T item);

    private void saveClicked() {
        if (binder.writeBeanIfValid(currentItem)) {
            close();
        }
    }

    @Override
    public void close() {
        super.close();
    }
}

