package com.github.kshashov.timetracker.web.mvc.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.service.admin.projects.ProjectsService;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import com.github.kshashov.timetracker.web.mvc.MainLayout;
import com.github.kshashov.timetracker.web.mvc.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.mvc.components.detail.Detail;
import com.github.kshashov.timetracker.web.mvc.components.detail.DetailHeader;
import com.github.kshashov.timetracker.web.mvc.layout.size.Horizontal;
import com.github.kshashov.timetracker.web.mvc.layout.size.Top;
import com.github.kshashov.timetracker.web.mvc.util.DataHandler;
import com.github.kshashov.timetracker.web.mvc.util.css.BoxSizing;
import com.github.kshashov.timetracker.web.mvc.util.css.FlexDirection;
import com.github.kshashov.timetracker.web.mvc.views.MasterDetail;
import com.github.kshashov.timetracker.web.mvc.views.admin.projects.dialogs.ProjectEditorDialog;
import com.github.kshashov.timetracker.web.security.HasUser;
import com.github.kshashov.timetracker.web.security.ProjectPermissionType;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "projects", layout = MainLayout.class)
@PageTitle("Projects")
public class Projects extends MasterDetail implements HasUser, DataHandler {

    private final ProjectsService projectsService;
    private final RolePermissionsHelper rolePermissionsHelper;

    private final User user;
    private final Span permissionsWarning = new Span("You do not have sufficient permissions to this project");

    private final UserProjectsView userProjectsView;
    private final ProjectActionsView projectActionsView;
    private final ProjectInfoView projectInfoView;
    private final ProjectUsersView projectUsersView;

    private Detail detailsDrawer;
    private DetailHeader detailsDrawerHeader;

    @Autowired
    public Projects(
            RolePermissionsHelper rolePermissionsHelper,
            ProjectsService projectsService,
            UserProjectsView userProjectsView,
            ProjectActionsView projectActionsView,
            ProjectUsersView projectUsersView,
            ProjectInfoView projectInfoView) {
        super(Position.RIGHT);
        setDetailSize(Size.LARGE);

        this.rolePermissionsHelper = rolePermissionsHelper;
        this.projectsService = projectsService;

        this.userProjectsView = userProjectsView;
        this.projectInfoView = projectInfoView;
        this.projectActionsView = projectActionsView;
        this.projectUsersView = projectUsersView;

        this.user = getUser();

        setViewContent(createContent());
        initDetailsDrawer();

        projectInfoView.setValidator((project, context) -> {
            var result = onProjectUpdated(project);
            return result ? ValidationResult.ok() : ValidationResult.error("error");
        });

        userProjectsView.addOnProjectSelectedEventListener(event -> {
            if (event.getProject() == null) {
                // Hide details if nothing selected
                detailsDrawer.collapse();
            } else {
                // Open project in drawer
                showProjectDetails(event.getProject(), event.getRole());
            }
        });

        resetDetails();

        userProjectsView.reloadProjects();
    }

    private Component createContent() {
        FlexBoxLayout content = new FlexBoxLayout(userProjectsView);
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

    private void showCreateProjectDialog(Project project) {
        new ProjectEditorDialog("Create Project", this::onProjectCreated).open(project);
    }

    private boolean onProjectCreated(Project project) {
        return handleDataManipulation(
                () -> projectsService.createProject(user, project),
                result -> userProjectsView.reloadProjects());
    }

    private boolean onProjectUpdated(Project project) {
        return handleDataManipulation(
                () -> projectsService.updateProject(user, project),
                result -> userProjectsView.reloadProjects());
    }

    private void resetDetails() {
        detailsDrawerHeader.setTitle("Empty");
        detailsDrawer.collapse();
        permissionsWarning.setVisible(false);
        projectInfoView.setVisible(false);
        projectActionsView.setVisible(false);
        projectUsersView.setVisible(false);
    }

    private void showProjectDetails(Project project, Role role) {
        detailsDrawerHeader.setTitle(project.getTitle());
        detailsDrawer.expand();

        showProjectInfo(project, role);
        showProjectActions(project, role);
        showProjectUsers(project, role);
    }

    private void showProjectInfo(Project project, Role role) {
        if (rolePermissionsHelper.hasPermission(role, ProjectPermissionType.EDIT_PROJECT_INFO)) {
            projectInfoView.setProject(project);
            permissionsWarning.setVisible(false);
            projectInfoView.setVisible(true);
        } else {
            permissionsWarning.setVisible(true);
            projectInfoView.setVisible(false);
        }
    }

    private void showProjectActions(Project project, Role role) {
        if (rolePermissionsHelper.hasPermission(role, ProjectPermissionType.EDIT_PROJECT_ACTIONS)) {
            projectActionsView.setProject(project);
            projectActionsView.setVisible(true);
        } else {
            projectActionsView.setVisible(false);
        }
    }

    private void showProjectUsers(Project project, Role role) {
        if (rolePermissionsHelper.hasPermission(role, ProjectPermissionType.EDIT_PROJECT_USERS)) {
            projectUsersView.setProject(project);
            projectUsersView.setVisible(true);
        } else {
            projectUsersView.setVisible(false);
        }
    }

    private Detail initDetailsDrawer() {
        detailsDrawer = getDetailsDrawer();

        // Header
        detailsDrawerHeader = new DetailHeader("");
        detailsDrawerHeader.setCanReset(false);
        detailsDrawerHeader.addCloseListener(buttonClickEvent -> {
            userProjectsView.deselectAll();
            detailsDrawer.collapse();
            resetDetails();
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
