package com.github.kshashov.timetracker.web.ui.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.web.ui.components.Widget;
import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.github.kshashov.timetracker.web.ui.views.admin.projects.dialogs.ProjectActionEditorDialog;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import rx.Subscription;

import java.util.ArrayList;
import java.util.List;

@Scope("prototype")
@SpringComponent
public class ProjectActionsWidget extends Widget {
    private final ProjectActionsViewModel viewModel;
    private List<Subscription> subscriptions = new ArrayList<>();

    private ProjectActionEditorDialog createActionDialog = new ProjectActionEditorDialog("Create Action");
    private ProjectActionEditorDialog editActionDialog = new ProjectActionEditorDialog("Edit Action");
    private final Grid<Action> actionsGrid = new Grid<>();

    @Autowired
    public ProjectActionsWidget(ProjectActionsViewModel viewModel) {
        this.viewModel = viewModel;

        initActions();
        initActionsGrid();
        setTitle("Actions");
    }

    private void initActions() {
        var plus = UIUtils.createTertiaryButton(VaadinIcon.PLUS_CIRCLE_O);
        plus.addClickListener(event -> viewModel.createAction());

        var refresh = UIUtils.createTertiaryButton(VaadinIcon.REFRESH);
        refresh.addClickListener(event -> viewModel.reloadActions());

        addActions(plus, refresh);
    }

    private void initActionsGrid() {
        actionsGrid.addColumn(Action::getTitle).setHeader("Action");
        actionsGrid.addColumn(new ComponentRenderer<>(action -> {
            var layout = new HorizontalLayout();

            var edit = UIUtils.createActionButton(VaadinIcon.PENCIL);
            edit.addClickListener(e -> viewModel.updateAction(action));
            var delete = UIUtils.createActionButton(VaadinIcon.FILE_REMOVE);
            delete.addClickListener(e -> viewModel.updateAction(action));

            layout.add(edit);
            layout.add(delete);
            return layout;
        })).setHeader("");
        actionsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        addContentItems(actionsGrid);
    }

    public void setProject(Project project, Role role) {
        viewModel.setProject(project, role);
    }

    private void reloadActions(List<Action> actions) {
        actionsGrid.setItems(actions);
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
