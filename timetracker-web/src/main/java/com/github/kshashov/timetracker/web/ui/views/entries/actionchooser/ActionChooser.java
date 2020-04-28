package com.github.kshashov.timetracker.web.ui.views.entries.actionchooser;

import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.web.ui.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.ui.layout.size.Uniform;
import com.github.kshashov.timetracker.web.ui.util.css.FlexDirection;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import java.util.ArrayList;
import java.util.List;

@Tag("action-chooser")
public class ActionChooser extends AbstractField<ActionChooser, Action> {
    private List<Project> projects;
    private Action action;

    private final FlexBoxLayout layout = new FlexBoxLayout();
    private final Select<Project> projectField = new Select<>();
    private final Select<Action> actionField = new Select<>();
    private boolean isPresentationValueUpdated;

    public ActionChooser(FlexDirection direction) {
        super(null);

        layout.setFlexDirection(direction);
        layout.setSpacing(Uniform.XS);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        initProjectField();
        initActionField();

        layout.add(projectField, actionField);
        getElement().appendChild(layout.getElement());
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
        projectField.setItems(projects);
        setValue(action);
    }

    private void initActionField() {
        actionField.setLabel("Action");
        actionField.getElement().setAttribute("theme", "small");
        actionField.setItemEnabledProvider(Action::getIsActive);
        actionField.setRenderer(new ComponentRenderer<>(action -> {
            return new Span(action.getTitle());
        }));
        actionField.addValueChangeListener(value -> {
            action = value.getValue();
            if (!isPresentationValueUpdated) {
                setModelValue(action, false);
            }
        });
    }

    private void initProjectField() {
        projectField.setLabel("Project");
        projectField.getElement().setAttribute("theme", "small");
        projectField.setItemEnabledProvider(Project::getIsActive);
        projectField.setRenderer(new ComponentRenderer<>(p -> {
            return new Span(p.getTitle());
        }));
        projectField.addValueChangeListener((value) -> {
            Action saved = action;

            List<Action> actions = value.getHasValue().isEmpty()
                    ? new ArrayList<>()
                    : value.getValue().getActions();

            actionField.setItems(actions);

            // Restore action value if possible
            updateAction(actions, saved);
        });
    }

    private void updateAction(List<Action> actions, Action current) {
        if (current != null) {
            // Restore action value
            action = actions.stream()
                    .filter(a -> a.getId().equals(current.getId()))
                    .findFirst().orElse(null);
        }

        actionField.setValue(action);
    }

    @Override
    protected void setPresentationValue(Action newPresentationValue) {
        isPresentationValueUpdated = true;
        action = newPresentationValue;

        // Reset fields if null
        if (newPresentationValue == null) {
            projectField.setValue(null);
            isPresentationValueUpdated = false;
            return;
        }

        // Set project to the field if any found
        projects.stream()
                .filter(p -> p.getId().equals(action.getProject().getId()))
                .findFirst()
                .ifPresent(projectField::setValue);

        isPresentationValueUpdated = false;
    }
}
