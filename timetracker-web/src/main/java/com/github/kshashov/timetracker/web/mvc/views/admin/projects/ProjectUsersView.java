package com.github.kshashov.timetracker.web.mvc.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.repo.user.ProjectRolesRepository;
import com.github.kshashov.timetracker.data.repo.user.RolesRepository;
import com.github.kshashov.timetracker.data.repo.user.UsersRepository;
import com.github.kshashov.timetracker.data.service.ProjectsAdminService;
import com.github.kshashov.timetracker.data.utils.OffsetLimitRequest;
import com.github.kshashov.timetracker.web.mvc.util.DataHandler;
import com.github.kshashov.timetracker.web.mvc.views.admin.projects.dialogs.ProjectUserCreatorDialog;
import com.github.kshashov.timetracker.web.mvc.views.admin.projects.dialogs.ProjectUserEditorDialog;
import com.github.kshashov.timetracker.web.security.SecurityUtils;
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
public class ProjectUsersView extends VerticalLayout implements DataHandler {
    private final ProjectsAdminService projectsAdminService;
    private final ProjectRolesRepository projectRolesRepository;
    private final UsersRepository usersRepository;
    private final Grid<ProjectRole> usersGrid = new Grid<>();
    private final List<Role> roles;
    private final User user;
    private Project project;

    @Autowired
    public ProjectUsersView(
            ProjectsAdminService projectsAdminService,
            ProjectRolesRepository projectRolesRepository,
            RolesRepository rolesRepository,
            UsersRepository usersRepository) {
        this.user = SecurityUtils.getCurrentUser().getUser();
        this.projectsAdminService = projectsAdminService;
        this.projectRolesRepository = projectRolesRepository;
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
            var action = new ProjectRole();
            action.setProject(project);
            new ProjectUserCreatorDialog("Add User", this::onUserRoleCreated, usersDataProvider, roles).open(action);
        });
        return button;
    }

    private Grid<ProjectRole> initUsersGrid() {
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

            if (!a.getUser().getId().equals(user.getId())) {
                var edit = new Button(VaadinIcon.PENCIL.create(),
                        (event) -> new ProjectUserEditorDialog("Edit User", this::onUserRoleUpdated, roles).open(a));

                layout.add(edit);
            }

            return layout;
        })).setHeader("").setSortable(false).setAutoWidth(true);
        usersGrid.setSelectionMode(Grid.SelectionMode.NONE);
        usersGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        return usersGrid;
    }

    private boolean onUserRoleCreated(ProjectRole projectRole) {
        return handleDataManipulation(projectsAdminService.createProjectRole(user, projectRole), projectRole1 -> reloadUsers());
    }

    private boolean onUserRoleUpdated(ProjectRole projectRole) {
        return handleDataManipulation(projectsAdminService.updateProjectRole(user, projectRole), projectRole1 -> reloadUsers());
    }

    private void reloadUsers() {
        Set<ProjectRole> users = projectRolesRepository.findProjectUsersWithRoles(project.getId());
        usersGrid.setItems(users);
    }

    public void setProject(Project project) {
        this.project = project;
        reloadUsers();
    }
}
