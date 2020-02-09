package com.github.kshashov.timetracker.web.mvc.views;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.ProjectRoles;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.repo.user.ProjectRolesRepository;
import com.github.kshashov.timetracker.data.repo.user.RolesRepository;
import com.github.kshashov.timetracker.data.service.ProjectsService;
import com.github.kshashov.timetracker.web.mvc.MainLayout;
import com.github.kshashov.timetracker.web.mvc.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.mvc.components.detailsdrawer.DetailsDrawer;
import com.github.kshashov.timetracker.web.mvc.components.detailsdrawer.DetailsDrawerHeader;
import com.github.kshashov.timetracker.web.mvc.layout.size.Horizontal;
import com.github.kshashov.timetracker.web.mvc.layout.size.Top;
import com.github.kshashov.timetracker.web.mvc.util.css.BoxSizing;
import com.github.kshashov.timetracker.web.mvc.util.css.FlexDirection;
import com.github.kshashov.timetracker.web.mvc.view.component.ProjectActionsView;
import com.github.kshashov.timetracker.web.mvc.view.component.ProjectInfoView;
import com.github.kshashov.timetracker.web.mvc.view.component.ProjectUsersView;
import com.github.kshashov.timetracker.web.mvc.view.component.dialog.ProjectEditorDialog;
import com.github.kshashov.timetracker.web.security.SecurityUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

@Route(value = "projects", layout = MainLayout.class)
@PageTitle("Projects")
public class Projects extends SplitViewFrame {

    private final ProjectsService projectsService;
    private final ProjectRolesRepository projectRolesRepository;
    private final User user;
    private final Grid<ProjectRoles> projectsGrid = new Grid<>();
    private final ListDataProvider<ProjectRoles> projectsDataProvider = new ListDataProvider<>(new ArrayList<>());
    private final Map<Long, Role> roles;
    private final Span permissionsWarning = new Span("You do not have sufficient permissions to this project");
    private final ProjectActionsView projectActionsView;
    private final ProjectInfoView projectInfoView;
    private final ProjectUsersView projectUsersView;

    private DetailsDrawer detailsDrawer;
    private DetailsDrawerHeader detailsDrawerHeader;
    private Project selectedProject;

    @Autowired
    public Projects(
            RolesRepository rolesRepository,
            ProjectsService projectsService,
            ProjectRolesRepository projectRolesRepository,
            ProjectActionsView projectActionsView,
            ProjectUsersView projectUsersView,
            ProjectInfoView projectInfoView) {
        this.projectsService = projectsService;
        this.projectRolesRepository = projectRolesRepository;

        this.projectInfoView = projectInfoView;
        this.projectActionsView = projectActionsView;
        this.projectUsersView = projectUsersView;

        this.user = SecurityUtils.getCurrentUser().getUser();
        this.roles = rolesRepository.findAllWithPermissions().stream()
                .collect(Collectors.toMap(Role::getId, r -> r));

        setViewContent(createContent());
        setViewDetails(createDetailsDrawer());

        reloadProjects();
    }

    private Component createContent() {
        FlexBoxLayout content = new FlexBoxLayout(createProjectsGrid());
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setFlexDirection(FlexDirection.COLUMN);
        content.setHeightFull();
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        return content;
    }

    private void onCreateNewProjectClicked() {
        var project = new Project();
        showCreateProjectDialog(project);
    }

    private Grid<ProjectRoles> createProjectsGrid() {
        projectsGrid.setDataProvider(projectsDataProvider);
        projectsGrid.setSizeFull();
        projectsGrid.addColumn(new ComponentRenderer<>(pr -> {
            var span = new Span(pr.getProject().getTitle());
            return span;
        })).setHeader("Project").setSortable(true).setComparator(Comparator.comparing(o -> o.getProject().getTitle())).setAutoWidth(true);
        projectsGrid.addColumn(new ComponentRenderer<>(pr -> {
            var span = new Span(pr.getRole().getTitle());
            return span;
        })).setHeader("Role").setComparator(Comparator.comparing(o -> o.getRole().getTitle())).setSortable(false).setAutoWidth(true).setFlexGrow(0);
        projectsGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        projectsGrid.addSelectionListener(selectionEvent -> selectionEvent.getFirstSelectedItem().ifPresent(projectRoles -> {
            showProjectDetails(projectRoles.getProject(), projectRoles.getRole());
        }));
        projectsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        return projectsGrid;
    }

    private void reloadProjects() {
        var rows = projectRolesRepository.findUserProjectsWithRoles(user.getId());
        projectsDataProvider.getItems().clear();
        projectsDataProvider.getItems().addAll(rows);
        projectsDataProvider.refreshAll();

        if (selectedProject != null) {
            // Save selection after reload
            rows.stream()
                    .filter(projectRoles -> projectRoles.getProject().getId().equals(selectedProject.getId()))
                    .findFirst()
                    .ifPresentOrElse(
                            projectsGrid::select, // select previous project
                            () -> selectedProject = null); // nothing selected
        } else if (rows.size() > 0) {
            // TODO Select first one?
        }

        // Hide details if nothing selected
        if (selectedProject == null) {
            detailsDrawer.hide();
        }

    }

    private void showCreateProjectDialog(Project project) {
        new ProjectEditorDialog("Create Project", updated -> {
            projectsService.createProject(updated, user);
            // TODO check operation result
            reloadProjects();
        }).open(project);
    }

    private void showProjectDetails(Project project, Role role) {
        detailsDrawerHeader.setTitle(project.getTitle());
        detailsDrawer.show();

        selectedProject = project;
        showProjectInfo(project, role);
        showProjectActions(project, role);
        showProjectUsers(project, role);
    }

    private void showProjectInfo(Project project, Role role) {
        if (roles.get(role.getId()).getPermissions().stream().noneMatch(p -> p.getCode().equalsIgnoreCase("edit_project_info"))) {
            permissionsWarning.setVisible(true);
            projectInfoView.setVisible(false);
        } else {
            projectInfoView.setProject(project);
            permissionsWarning.setVisible(false);
            projectInfoView.setVisible(true);
        }
    }

    private void showProjectActions(Project project, Role role) {
        if (roles.get(role.getId()).getPermissions().stream().noneMatch(p -> p.getCode().equalsIgnoreCase("edit_project_actions"))) {
            projectActionsView.setVisible(false);
        } else {
            projectActionsView.setProject(project);
            projectActionsView.setVisible(true);
        }
    }

    private void showProjectUsers(Project project, Role role) {
        if (roles.get(role.getId()).getPermissions().stream().noneMatch(p -> p.getCode().equalsIgnoreCase("edit_project_users"))) {
            projectUsersView.setVisible(false);
        } else {
            projectUsersView.setProject(project);
            projectUsersView.setVisible(true);
        }
    }

    private DetailsDrawer createDetailsDrawer() {
        detailsDrawer = new DetailsDrawer(DetailsDrawer.Position.RIGHT);

        // Header
        detailsDrawerHeader = new DetailsDrawerHeader("");
        detailsDrawerHeader.addCloseListener(buttonClickEvent -> {
            detailsDrawer.hide();
            projectsGrid.deselectAll();
        });
        detailsDrawer.setHeader(detailsDrawerHeader);
        detailsDrawer.setContent(createDetailsContent());

        return detailsDrawer;
    }

    private Component createDetailsContent() {
        FlexBoxLayout layout = new FlexBoxLayout(permissionsWarning, projectInfoView, projectActionsView, projectUsersView);
        layout.setFlexDirection(FlexDirection.COLUMN);
        return layout;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        var button = MainLayout.get().getAppBar().addActionItem(VaadinIcon.FILE_ADD, "New Project");
        button.addClickListener(event -> onCreateNewProjectClicked());
    }
}
