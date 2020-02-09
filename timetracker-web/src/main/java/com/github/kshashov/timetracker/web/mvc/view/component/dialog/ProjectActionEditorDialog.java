package com.github.kshashov.timetracker.web.mvc.view.component.dialog;

import com.github.kshashov.timetracker.data.entity.Action;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.logging.log4j.util.Strings;

import java.util.function.Consumer;

public class ProjectActionEditorDialog extends AbstractEditorDialog<Action> {
    private final TextField title = new TextField("Title");

    public ProjectActionEditorDialog(String title, Consumer<Action> itemSaver) {
        super(title, itemSaver);
        getFormLayout().add(createTitleField());
    }

    protected TextField createTitleField() {
        title.setRequiredIndicatorVisible(false);
        //baseVolume.setReadOnly(true);
        getBinder().forField(title).withNullRepresentation("")
                .withValidator(Strings::isNotBlank, "Title is empty")
                .bind(Action::getTitle, Action::setTitle);

        return title;
    }


    @Override
    protected void onDialogOpened(Action item) {

    }
}
