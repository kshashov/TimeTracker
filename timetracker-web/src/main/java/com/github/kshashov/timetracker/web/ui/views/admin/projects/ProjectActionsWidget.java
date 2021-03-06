package com.github.kshashov.timetracker.web.ui.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.web.ui.components.ConfirmDialog;
import com.github.kshashov.timetracker.web.ui.components.Widget;
import com.github.kshashov.timetracker.web.ui.util.CrudEntity;
import com.github.kshashov.timetracker.web.ui.util.HasSubscriptions;
import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.github.kshashov.timetracker.web.ui.views.admin.projects.dialogs.ProjectActionEditorDialog;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import rx.Subscription;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SpringComponent
public class ProjectActionsWidget extends Widget implements HasSubscriptions {
    private final ProjectActionsViewModel viewModel;
    private final List<Subscription> subscriptions = new ArrayList<>();

    private CrudEntity.CrudAccess access;

    private ProjectActionEditorDialog createActionDialog = new ProjectActionEditorDialog("Create Action");
    private ProjectActionEditorDialog editActionDialog = new ProjectActionEditorDialog("Edit Action");
    private final Grid<Action> actionsGrid = new Grid<>();
    private Button createAction = UIUtils.createTertiaryButton(VaadinIcon.PLUS_CIRCLE_O);
    private final ConfirmDialog deleteDialog = new ConfirmDialog("Delete", "Are you sure you want to delete this action?");
    private final ConfirmDialog activateDialog = new ConfirmDialog("Enable", "Are you sure you want to enable this action?");

    @Autowired
    public ProjectActionsWidget(ProjectActionsViewModel viewModel) {
        this.viewModel = viewModel;

        initActions();
        initActionsGrid();
        setTitle("Actions");
    }

    private void initActions() {
        createAction.addClickListener(event -> viewModel.createAction());

        var refresh = UIUtils.createTertiaryButton(VaadinIcon.REFRESH);
        refresh.addClickListener(event -> viewModel.reloadActions());

        addActions(createAction, refresh);
    }

    private void initActionsGrid() {
        actionsGrid.addColumn(new ComponentRenderer<>(action -> {
            return UIUtils.createActiveLabel(action.getTitle(), action.getIsActive());
        })).setHeader("Action").setSortable(true).setComparator(Comparator.comparing(Action::getTitle));
        actionsGrid.addColumn(new ComponentRenderer<>(action -> {
            var layout = new HorizontalLayout();

            if (access.canEdit() && action.getIsActive()) {
                var edit = UIUtils.createActionButton(VaadinIcon.PENCIL);
                edit.addClickListener(e -> viewModel.updateAction(action));
                layout.add(edit);
            }

            if (access.canDelete() && action.getIsActive()) {
                var delete = UIUtils.createActionButton(VaadinIcon.MINUS_CIRCLE_O);
                delete.addClickListener(e -> deleteDialog.open(b -> {
                    if (b) viewModel.deleteAction(action);
                }));
                layout.add(delete);
            }

            if (access.canEnable() && !action.getIsActive()) {
                var activate = UIUtils.createActionButton("Activate");
                activate.addClickListener(e -> activateDialog.open(b -> {
                    if (b) viewModel.activateAction(action);
                }));
                layout.add(activate);
            }

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

        subscribe(viewModel.actions().
                subscribe(actions -> {
                    access = actions.getAccess();
                    if (access.canView()) {
                        setVisible(true);
                        createAction.setVisible(access.canCreate());
                        reloadActions(actions.getEntity());
                    } else {
                        setVisible(false);
                    }
                }));

        subscribe(viewModel.createActionDialogs()
                .subscribe(actionDialog -> {
                    createActionDialog.open(actionDialog.getAction(), actionDialog.getValidator());
                }));

        subscribe(viewModel.updateActionDialogs()
                .subscribe(actionDialog -> {
                    editActionDialog.open(actionDialog.getAction(), actionDialog.getValidator());
                }));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        unsubscribeAll();
    }

    @Override
    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }
}
