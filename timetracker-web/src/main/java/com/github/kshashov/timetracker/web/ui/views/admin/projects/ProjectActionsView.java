package com.github.kshashov.timetracker.web.ui.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.github.kshashov.timetracker.web.ui.views.admin.projects.dialogs.ProjectActionEditorDialog;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import rx.Subscription;

import java.util.ArrayList;
import java.util.List;

@UIScope
@SpringComponent
public class ProjectActionsView extends VerticalLayout {
    private final ProjectActionsViewModel viewModel;
    private List<Subscription> subscriptions = new ArrayList<>();

    private ProjectActionEditorDialog createActionDialog = new ProjectActionEditorDialog("Create Action");
    private ProjectActionEditorDialog editActionDialog = new ProjectActionEditorDialog("Edit Action");
    private final Grid<Action> actionGrid = new Grid<>();

    @Autowired
    public ProjectActionsView(ProjectActionsViewModel viewModel) {
        this.viewModel = viewModel;
        add(new H4("Actions"));
        add(initNewActionButton());
        add(initGrid());
    }

    private Button initNewActionButton() {
        var button = UIUtils.createButton("New Action", VaadinIcon.FILE_ADD);
        button.addClickListener(event -> viewModel.createAction());
        return button;
    }

    private Grid<Action> initGrid() {
        actionGrid.addColumn(Action::getTitle).setHeader("Action");
        actionGrid.addColumn(new ComponentRenderer<>(action -> {
            var layout = new HorizontalLayout();

            var edit = UIUtils.createActionButton(VaadinIcon.PENCIL);
            edit.addClickListener(e -> viewModel.updateAction(action));
            var delete = UIUtils.createActionButton(VaadinIcon.FILE_REMOVE);
            delete.addClickListener(e -> viewModel.updateAction(action));

            layout.add(edit);
            layout.add(delete);
            return layout;
        })).setHeader("");
        actionGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        return actionGrid;
    }

    public void setProject(Project project, Role role) {
        viewModel.setProject(project, role);
    }

    private void reloadActions(List<Action> actions) {
        actionGrid.setItems(actions);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        subscriptions.add(viewModel.actions().
                subscribe(this::reloadActions));

        subscriptions.add(viewModel.createActionDialogs()
                .subscribe(actionDialog -> {
                    createActionDialog.open(actionDialog.getAction(), actionDialog.getValidator());
                }));

        subscriptions.add(viewModel.updateActionDialogs()
                .subscribe(actionDialog -> {
                    editActionDialog.open(actionDialog.getAction(), actionDialog.getValidator());
                }));

        subscriptions.add(viewModel.hasAccess()
                .subscribe(this::setVisible));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        subscriptions.forEach(Subscription::unsubscribe);
        subscriptions.clear();
    }
}
