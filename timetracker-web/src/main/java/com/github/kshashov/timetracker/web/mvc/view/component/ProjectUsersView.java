package com.github.kshashov.timetracker.web.mvc.view.component;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.ProjectRoles;
import com.github.kshashov.timetracker.data.entity.user.ProjectRolesIdentity;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.repo.user.ProjectRolesRepository;
import com.github.kshashov.timetracker.data.repo.user.RolesRepository;
import com.github.kshashov.timetracker.data.repo.user.UsersRepository;
import com.github.kshashov.timetracker.data.utils.OffsetLimitRequest;
import com.github.kshashov.timetracker.web.mvc.view.component.dialog.ProjectUserCreatorDialog;
import com.github.kshashov.timetracker.web.mvc.view.component.dialog.ProjectUserEditorDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Scope("prototype")
@SpringComponent
public class ProjectUsersView extends VerticalLayout {
    private final ProjectRolesRepository projectRolesRepository;
    private final RolesRepository rolesRepository;
    private final UsersRepository usersRepository;
    private final Grid<ProjectRoles> usersGrid = new Grid<>();
    private final List<Role> roles;
    private Project project;

    @Autowired
    public ProjectUsersView(ProjectRolesRepository projectRolesRepository, RolesRepository rolesRepository, UsersRepository usersRepository) {
        this.projectRolesRepository = projectRolesRepository;
        this.rolesRepository = rolesRepository;
        this.usersRepository = usersRepository;
        // TODO except inactive?
        this.roles = rolesRepository.findAll();
        add(new H4("Users"));
        add(initNewUserRoleButton());
        add(initUsersGrid());
    }

    private Button initNewUserRoleButton() {
        final CallbackDataProvider<User, String> usersDataProvider = new CallbackDataProvider<>(
                query -> {
                    // TODO filter users for project and string and count
                    return usersRepository.findAll(query.getFilter().orElse(""), new OffsetLimitRequest(query.getOffset(), query.getLimit())).getContent().stream();
                },
                query -> {
                    // TODO filter users for project and string and count
                    return Math.toIntExact(usersRepository.findAll(query.getFilter().orElse(""), new OffsetLimitRequest(query.getOffset(), query.getLimit())).getNumberOfElements());
                });


        var button = new Button("Add User", event -> {
            var action = new ProjectRoles();
            action.setProject(project);
            new ProjectUserCreatorDialog("Add User", this::onUserRoleSaved, usersDataProvider, roles).open(action);
        });
        return button;
    }

    private Grid<ProjectRoles> initUsersGrid() {
        usersGrid.setWidthFull();
        usersGrid.addColumn(new ComponentRenderer<>(pr -> {
            var span = new Span(pr.getUser().getName());
            return span;
        })).setHeader("User").setSortable(true).setComparator(Comparator.comparing(o -> o.getUser().getName())).setResizable(true);
        usersGrid.addColumn(new ComponentRenderer<>(pr -> {
            var span = new Span(pr.getRole().getTitle());
            return span;
        })).setHeader("Role").setSortable(false).setAutoWidth(true);
        usersGrid.addColumn(new ComponentRenderer<>(a -> {
            var layout = new HorizontalLayout();

            var edit = new Button(
                    VaadinIcon.PENCIL.create(),
                    (event) -> new ProjectUserEditorDialog("Edit User", this::onUserRoleSaved, roles).open(a));

            layout.add(edit);
            return layout;
        })).setHeader("").setSortable(false).setAutoWidth(true);
        usersGrid.setSelectionMode(Grid.SelectionMode.NONE);
        usersGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        return usersGrid;
    }

    private void onUserRoleAdded(ProjectRoles projectRoles) {
        // TODO check if already existing
        onUserRoleSaved(projectRoles);
    }

    private void onUserRoleSaved(ProjectRoles projectRoles) {
        var id = new ProjectRolesIdentity();
        id.setUserId(projectRoles.getUser().getId());
        id.setProjectId(projectRoles.getProject().getId());
        projectRoles.setPermissionIdentity(id);
        projectRolesRepository.save(projectRoles);
        // TODO check result
        reloadUsers();
    }

    private void reloadUsers() {
        Set<ProjectRoles> users = projectRolesRepository.findProjectUsersWithRoles(project.getId());
        usersGrid.setItems(users);
    }

    public void setProject(Project project) {
        this.project = project;
        reloadUsers();
    }
}
