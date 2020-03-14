package com.github.kshashov.timetracker.web.ui.views.admin.projects.dialogs;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.web.ui.components.AbstractEditorDialog;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.logging.log4j.util.Strings;

public class ProjectEditorDialog extends AbstractEditorDialog<Project> {
    private final TextField title = new TextField("Title");

    public ProjectEditorDialog(String title) {
        super(title);
        getFormLayout().add(createTitleField());
    }

    protected TextField createTitleField() {
        title.setRequiredIndicatorVisible(false);
        getBinder().forField(title).withNullRepresentation("")
                .withValidator(Strings::isNotBlank, "Title is empty")
                .bind(Project::getTitle, Project::setTitle);

        return title;
    }

    @Override
    protected void onDialogOpened(Project item) {

    }
}
