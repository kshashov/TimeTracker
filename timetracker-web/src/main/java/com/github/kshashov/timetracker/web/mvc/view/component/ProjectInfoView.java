package com.github.kshashov.timetracker.web.mvc.view.component;

import com.github.kshashov.timetracker.data.entity.Project;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;
import lombok.Getter;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.annotation.Scope;

import javax.validation.constraints.NotNull;

@Scope("prototype")
@SpringComponent
public class ProjectInfoView extends VerticalLayout {
    private final Binder<Project> binder = new Binder<>();

    public ProjectInfoView() {
        TextField title = new TextField("Title");
        Button save = new Button("Save", (event -> {
            if (binder.isValid()) {
                var project = binder.getBean();
                fireEvent(new ProjectUpdatedEvent(this, project));
            }
        }));
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
        binder.setBean(project);
    }

    public Registration addOnProjectUpdatedEventListener(@NotNull ComponentEventListener<ProjectUpdatedEvent> listener) {
        return addListener(ProjectUpdatedEvent.class, listener);
    }

    @Getter
    public static class ProjectUpdatedEvent extends ComponentEvent<ProjectInfoView> {
        private final Project project;

        public ProjectUpdatedEvent(ProjectInfoView source, Project project) {
            super(source, false);
            this.project = project;
        }
    }
}
