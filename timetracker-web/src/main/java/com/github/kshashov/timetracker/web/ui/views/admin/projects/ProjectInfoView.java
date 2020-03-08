package com.github.kshashov.timetracker.web.ui.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.Project;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.annotation.Scope;

@Scope("prototype")
@SpringComponent
public class ProjectInfoView extends VerticalLayout {
    private final Binder<Project> binder = new Binder<>();
    private Project project;

    public ProjectInfoView() {
        TextField title = new TextField("Title");
        Button save = new Button("Save", (event -> binder.writeBeanIfValid(project)));
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("25em", 2));
        formLayout.add(title);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        binder.forField(title).withNullRepresentation("")
                .withValidator(Strings::isNotBlank, "Title is empty")
                .bind(Project::getTitle, Project::setTitle);
        add(formLayout);
        add(save);
    }

    public void setProject(Project project) {
        this.project = project;
        binder.readBean(project);
    }

    public boolean isDirty() {
        return binder.hasChanges();
    }

    public void setValidator(Validator<? super Project> validator) {
        binder.withValidator(validator);
    }
}
