package com.github.kshashov.timetracker.web.mvc.view.component.dialog;

import com.github.kshashov.timetracker.data.entity.Project;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.logging.log4j.util.Strings;

import java.util.function.Consumer;

public class ProjectEditorDialog extends AbstractEditorDialog<Project> {
    private final TextField title = new TextField("Title");

    public ProjectEditorDialog(String title, Consumer<Project> itemSaver) {
        super(title, itemSaver);
        getFormLayout().add(createTitleField());
    }

    protected TextField createTitleField() {
        title.setRequiredIndicatorVisible(false);
        //baseVolume.setReadOnly(true);
        getBinder().forField(title).withNullRepresentation("")
                .withValidator(Strings::isNotBlank, "Title is empty")
                .bind(Project::getTitle, Project::setTitle);

        return title;
    }


    @Override
    protected void onDialogOpened(Project item) {

    }
}
