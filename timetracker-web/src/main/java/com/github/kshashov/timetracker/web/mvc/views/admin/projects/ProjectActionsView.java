package com.github.kshashov.timetracker.web.mvc.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.repo.ActionsRepository;
import com.github.kshashov.timetracker.data.service.admin.projects.ProjectActionsService;
import com.github.kshashov.timetracker.web.mvc.util.DataHandler;
import com.github.kshashov.timetracker.web.mvc.views.admin.projects.dialogs.ProjectActionEditorDialog;
import com.github.kshashov.timetracker.web.security.SecurityUtils;
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

import java.util.List;

@Scope("prototype")
@SpringComponent
public class ProjectActionsView extends VerticalLayout implements DataHandler {
    private final ProjectActionsService actionsService;
    private final ActionsRepository actionsRepository;
    private final Grid<Action> actionGrid = new Grid<>();
    private final User user;
    private Project project;

    @Autowired
    public ProjectActionsView(ProjectActionsService actionsService, ActionsRepository actionsRepository) {
        this.actionsService = actionsService;
        this.actionsRepository = actionsRepository;
        this.user = SecurityUtils.getCurrentUser().getUser();
        add(new H4("Actions"));
        add(initNewActionButton());
        add(initGrid());
    }

    private Button initNewActionButton() {
        var button = new Button("New Action", event -> {
            var action = new Action();
            action.setProject(project);
            action.setIsActive(true);
            new ProjectActionEditorDialog("Create Action", this::onActionCreated).open(action);
        });
        return button;
    }

    private Grid<Action> initGrid() {
        actionGrid.addColumn(Action::getTitle).setHeader("Action");
        actionGrid.addColumn(new ComponentRenderer<>(a -> {
            var layout = new HorizontalLayout();

            var edit = new Button(
                    VaadinIcon.PENCIL.create(),
                    (event) -> new ProjectActionEditorDialog("Edit Action", this::onActionUpdated).open(a));
            var delete = new Button(
                    VaadinIcon.FILE_REMOVE.create(),
                    (event) -> new ProjectActionEditorDialog("Delete Action", this::onActionUpdated).open(a));

            layout.add(edit);
            layout.add(delete);
            return layout;
        })).setHeader("");
        actionGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        return actionGrid;
    }

    private boolean onActionCreated(Action action) {
        return handleDataManipulation(
                () -> actionsService.createAction(user, action),
                result -> reloadActions());
    }

    private boolean onActionUpdated(Action action) {
        return handleDataManipulation(
                () -> actionsService.updateAction(user, action),
                result -> reloadActions());
    }

    private void reloadActions() {
        List<Action> actions = actionsRepository.findAllByProject(project.getId());
        actionGrid.setItems(actions);
    }

    public void setProject(Project project) {
        this.project = project;
        reloadActions();
    }
}
