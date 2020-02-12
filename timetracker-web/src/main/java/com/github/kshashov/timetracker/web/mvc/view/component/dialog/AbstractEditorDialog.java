package com.github.kshashov.timetracker.web.mvc.view.component.dialog;

import com.github.kshashov.timetracker.web.mvc.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.mvc.components.detailsdrawer.DetailsDrawerFooter;
import com.github.kshashov.timetracker.web.mvc.layout.size.Horizontal;
import com.github.kshashov.timetracker.web.mvc.layout.size.Vertical;
import com.github.kshashov.timetracker.web.mvc.util.css.BoxSizing;
import com.github.kshashov.timetracker.web.mvc.util.css.FlexDirection;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.shared.Registration;

import java.util.function.Consumer;

@CssImport(value = "frontend://styles/styles.css", themeFor = "vaadin-dialog-overlay")
public abstract class AbstractEditorDialog<T> extends Dialog {

    private final H3 titleField = new H3();
    private final FlexBoxLayout content = new FlexBoxLayout();
    private final DetailsDrawerFooter footer = new DetailsDrawerFooter();
    private final Button saveButton = footer.getSave();
    private final Button cancelButton = footer.getCancel();
    private final FormLayout formLayout = new FormLayout();
    private final String title;
    private final Consumer<T> itemSaver;
    private Registration registrationForSave;
    private Registration saveShortcutRegistration;
    private Binder<T> binder = new Binder<>();
    private T currentItem;

    protected AbstractEditorDialog(String title, Consumer<T> itemSaver) {
        this.title = title;
        this.itemSaver = itemSaver;

        getElement().setAttribute("theme", "no-padding-dialog");

        initContent();
        initButtonBar();
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);

    }

    private void initContent() {
        content.setFlexDirection(FlexDirection.COLUMN);
        content.setMargin();
        content.setPadding(Horizontal.L, Vertical.L);

        initTitle();
        initFormLayout();
        add(content);
    }

    private void initTitle() {
        content.add(titleField);
    }

    private void initFormLayout() {
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("25em", 2));
        Div div = new Div(formLayout);
        div.addClassName("has-padding");
        content.add(div);
    }

    private void initButtonBar() {
        footer.setBoxSizing(BoxSizing.BORDER_BOX);
        saveButton.setAutofocus(true);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addClickListener(e -> close());
        add(footer);
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
        registrationForSave = saveButton.addClickListener(e -> saveClicked());
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
        saveShortcutRegistration = saveButton.addClickShortcut(Key.ENTER);
    }

    private void disableShortcuts() {
        if (saveShortcutRegistration != null) {
            saveShortcutRegistration.remove();
            saveShortcutRegistration = null;
        }
    }
}

