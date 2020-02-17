package com.github.kshashov.timetracker.web.mvc.view.component.dialog;

import com.github.kshashov.timetracker.web.mvc.components.ButtonsDialog;
import com.github.kshashov.timetracker.web.mvc.util.UIUtils;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.shared.Registration;

import java.util.function.Consumer;

public abstract class AbstractEditorDialog<T> extends ButtonsDialog {

    private final H3 titleField = new H3();
    private final FormLayout formLayout = new FormLayout();
    private final Button save = UIUtils.createPrimaryButton("Save");
    private final Button cancel = UIUtils.createTertiaryButton("Cancel");
    private final String title;
    private final Consumer<T> itemSaver;
    private Registration registrationForSave;
    private Registration saveShortcutRegistration;
    private Binder<T> binder = new Binder<>();
    private T currentItem;

    protected AbstractEditorDialog(String title, Consumer<T> itemSaver) {
        this.title = title;
        this.itemSaver = itemSaver;

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
    }

    private void initFormLayout() {
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
        currentItem = item;
        titleField.setText(title);
        if (registrationForSave != null) {
            registrationForSave.remove();
        }
        registrationForSave = save.addClickListener(e -> saveClicked());
        onDialogOpened(item);
        binder.readBean(currentItem);

        enableShortcuts();

        open();
    }

    protected abstract void onDialogOpened(T item);

    private void saveClicked() {
        boolean isValid = binder.writeBeanIfValid(currentItem);

        if (isValid) {
            itemSaver.accept(currentItem);
            close();
        } else {
            BinderValidationStatus<T> status = binder.validate();
        }
    }

    @Override
    public void close() {
        super.close();
        disableShortcuts();
    }

    private void enableShortcuts() {
        disableShortcuts();
        saveShortcutRegistration = save.addClickShortcut(Key.ENTER);
    }

    private void disableShortcuts() {
        if (saveShortcutRegistration != null) {
            saveShortcutRegistration.remove();
            saveShortcutRegistration = null;
        }
    }
}

