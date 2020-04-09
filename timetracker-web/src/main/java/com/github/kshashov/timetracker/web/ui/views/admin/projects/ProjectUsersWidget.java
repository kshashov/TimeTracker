package com.github.kshashov.timetracker.web.ui.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.web.security.HasUser;
import com.github.kshashov.timetracker.web.ui.components.ConfirmDialog;
import com.github.kshashov.timetracker.web.ui.components.RoleBadge;
import com.github.kshashov.timetracker.web.ui.components.Widget;
import com.github.kshashov.timetracker.web.ui.util.CrudEntity;
import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.github.kshashov.timetracker.web.ui.views.admin.projects.dialogs.ProjectRoleCreatorDialog;
import com.github.kshashov.timetracker.web.ui.views.admin.projects.dialogs.ProjectRoleEditorDialog;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import rx.Subscription;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Scope("prototype")
@SpringComponent
public class ProjectUsersWidget extends Widget implements HasUser {
    private final ProjectUsersViewModel viewModel;
    private List<Subscription> subscriptions = new ArrayList<>();

    private final User user;
    private CrudEntity.CrudAccess access;

    private final Grid<ProjectRole> usersGrid = new Grid<>();
    private final Button createProjectRole = UIUtils.createTertiaryButton(VaadinIcon.PLUS_CIRCLE_O);
    private final ConfirmDialog confirmDialog = new ConfirmDialog("Delete", "Are you sure you wan to delete this user role?");

    @Autowired
    public ProjectUsersWidget(ProjectUsersViewModel viewModel) {
        this.user = getUser();
        this.viewModel = viewModel;

        initAction();
        initUsersGrid();
        setTitle("Users");
    }

    private void initAction() {
        createProjectRole.addClickListener(event -> viewModel.createProjectRole());

        var refresh = UIUtils.createTertiaryButton(VaadinIcon.REFRESH);
        refresh.addClickListener(event -> viewModel.reloadUsers());

        addActions(createProjectRole, refresh);
    }

    private void initUsersGrid() {
        usersGrid.setWidthFull();
        usersGrid.addColumn(new ComponentRenderer<>(pr -> {
            var span = new Span(pr.getUser().getName());
            return span;
        })).setHeader("User").setSortable(true).setComparator(Comparator.comparing(o -> o.getUser().getName())).setResizable(true);
        usersGrid.addColumn(new ComponentRenderer<>(pr -> {
            return new RoleBadge(pr.getRole());
        })).setHeader("Role").setSortable(false).setAutoWidth(true);
        usersGrid.addColumn(new ComponentRenderer<>(a -> {
            var layout = new HorizontalLayout();

            if (a.getUser().getId().equals(user.getId())) {
                return layout;
            }

            if (access.canEdit()) {
                var edit = UIUtils.createActionButton(VaadinIcon.PENCIL);
                edit.addClickListener(e -> viewModel.updateProjectRole(a));
                layout.add(edit);
            }

            if (access.canDelete()) {
                var delete = UIUtils.createActionButton(VaadinIcon.FILE_REMOVE);
                delete.addClickListener(e -> confirmDialog.open(b -> {
                    if (b) viewModel.deleteProjectRole(a);
                }));
                layout.add(delete);
            }

            return layout;
        })).setHeader("").setSortable(false).setAutoWidth(true);

        usersGrid.setSelectionMode(Grid.SelectionMode.NONE);
        usersGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        addContentItems(usersGrid);
    }

    public void setProject(Project project, Role role) {
        viewModel.setProject(project, role);
    }

    private void reloadUsers(List<ProjectRole> projectRoles) {
        usersGrid.setItems(projectRoles);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        subscriptions.add(viewModel.projectRoles()
                .subscribe(projectRoles -> {
                    access = projectRoles.getAccess();
                    if (access.canView()) {
                        setVisible(true);
                        createProjectRole.setVisible(access.canCreate());
                        reloadUsers(projectRoles.getEntity());
                    } else {
                        setVisible(false);
                    }
                }));

        subscriptions.add(viewModel.createRoleDialogs()
                .subscribe(projectRoleDialog -> {
                    ProjectRoleCreatorDialog dialog = new ProjectRoleCreatorDialog("Create Role", projectRoleDialog.getValidator(), projectRoleDialog.getRoles(), projectRoleDialog.getUsersDataProvider());
                    dialog.open(projectRoleDialog.getProjectRole());
                }));

        subscriptions.add(viewModel.updateRoleDialogs()
                .subscribe(projectRoleDialog -> {
                    ProjectRoleEditorDialog dialog = new ProjectRoleEditorDialog("Edit Role", projectRoleDialog.getValidator(), projectRoleDialog.getRoles());
                    dialog.open(projectRoleDialog.getProjectRole());
                }));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        subscriptions.forEach(Subscription::unsubscribe);
        subscriptions.clear();
    }
}
