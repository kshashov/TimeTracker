package com.github.kshashov.timetracker.web.mvc.view.component;

import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.repo.ActionsRepository;
import com.github.kshashov.timetracker.web.mvc.view.component.dialog.ProjectActionEditorDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import java.util.Set;

@Scope("prototype")
@SpringComponent
public class ProjectActionsView extends VerticalLayout {
    private final ActionsRepository actionsRepository;
    private final Grid<Action> actionGrid = new Grid<>();
    private Project project;

    @Autowired
    public ProjectActionsView(ActionsRepository actionsRepository) {
        this.actionsRepository = actionsRepository;
        add(new H4("Actions"));
        add(initNewActionButton());
        add(initGrid());
    }

    private Button initNewActionButton() {
        var button = new Button("New Action", event -> {
            var action = new Action();
            action.setProject(project);
            action.setIsActive(true);
            new ProjectActionEditorDialog("Create Action", this::onActionSaved).open(action);
        });
        return button;
    }

    private Grid<Action> initGrid() {
        actionGrid.addColumn(Action::getTitle).setHeader("Action");
        actionGrid.addColumn(new ComponentRenderer<>(a -> {
            var layout = new HorizontalLayout();

            var edit = new Button(
                    VaadinIcon.PENCIL.create(),
                    (event) -> new ProjectActionEditorDialog("Edit Action", this::onActionSaved).open(a));
            var delete = new Button(
                    VaadinIcon.FILE_REMOVE.create(),
                    (event) -> new ProjectActionEditorDialog("Delete Action", this::onActionSaved).open(a));

            layout.add(edit);
            layout.add(delete);
            return layout;
        })).setHeader("");
        actionGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        return actionGrid;
    }

    private void onActionSaved(Action action) {
        actionsRepository.save(action);
        // TODO check result
        reloadActions();
    }

    private void reloadActions() {
        Set<Action> actions = actionsRepository.findAllByProject(project);
        actionGrid.setItems(actions);
    }

    public void setProject(Project project) {
        this.project = project;
        reloadActions();
    }
}
