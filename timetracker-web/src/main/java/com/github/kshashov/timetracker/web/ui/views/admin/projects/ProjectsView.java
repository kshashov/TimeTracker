package com.github.kshashov.timetracker.web.ui.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.web.ui.MainLayout;
import com.github.kshashov.timetracker.web.ui.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.ui.components.detail.Detail;
import com.github.kshashov.timetracker.web.ui.components.detail.DetailHeader;
import com.github.kshashov.timetracker.web.ui.layout.size.Horizontal;
import com.github.kshashov.timetracker.web.ui.layout.size.Top;
import com.github.kshashov.timetracker.web.ui.util.css.BoxSizing;
import com.github.kshashov.timetracker.web.ui.util.css.FlexDirection;
import com.github.kshashov.timetracker.web.ui.views.MasterDetail;
import com.github.kshashov.timetracker.web.ui.views.admin.projects.dialogs.ProjectEditorDialog;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "projects", layout = MainLayout.class)
@PageTitle("Projects")
public class ProjectsView extends MasterDetail {
    private final ProjectSelectorViewModel projectSelectorViewModel;
    private final ProjectsAdminViewModel projectsAdminViewModel;

    private final UserProjectsView userProjectsView;
    private final ProjectActionsView projectActionsView;
    private final ProjectInfoView projectInfoView;
    private final ProjectRolesView projectUsersView;
    private final ProjectEditorDialog createProjectDialog = new ProjectEditorDialog("Create Project");
    private final Detail detailsDrawer = getDetailsDrawer();
    private final DetailHeader detailsDrawerHeader = new DetailHeader("");

    @Autowired
    public ProjectsView(
            ProjectSelectorViewModel projectSelectorViewModel,
            ProjectsAdminViewModel projectsAdminViewModel,
            UserProjectsView userProjectsView,
            ProjectActionsView projectActionsView,
            ProjectRolesView projectUsersView,
            ProjectInfoView projectInfoView) {
        super(Position.RIGHT);
        this.projectSelectorViewModel = projectSelectorViewModel;
        this.projectsAdminViewModel = projectsAdminViewModel;

        this.userProjectsView = userProjectsView;
        this.projectInfoView = projectInfoView;
        this.projectActionsView = projectActionsView;
        this.projectUsersView = projectUsersView;

        setDetailSize(Size.LARGE);
        setViewContent(createContent());
        initDetailsDrawer();
    }

    private Component createContent() {
        FlexBoxLayout content = new FlexBoxLayout(userProjectsView);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setFlexDirection(FlexDirection.COLUMN);
        content.setHeightFull();
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        return content;
    }

    private void resetDetails() {
        detailsDrawerHeader.setTitle("Empty");
        detailsDrawer.collapse();

        projectInfoView.setVisible(false);
        projectActionsView.setVisible(false);
        projectUsersView.setVisible(false);
    }

    private void showProjectDetails(Project project, Role role) {
        detailsDrawerHeader.setTitle(project.getTitle());
        detailsDrawer.expand();

        projectInfoView.setProject(project, role);
        projectActionsView.setProject(project, role);
        projectUsersView.setProject(project, role);
    }

    private void initDetailsDrawer() {
        // Header
        detailsDrawerHeader.setCanReset(false);
        detailsDrawerHeader.addCloseListener(buttonClickEvent -> {
            userProjectsView.deselectAll();
            detailsDrawer.collapse();
//            resetDetails();
        });
        detailsDrawer.setHeader(detailsDrawerHeader);
        detailsDrawer.setContent(createDetailsContent());
    }

    private Component createDetailsContent() {
        FlexBoxLayout layout = new FlexBoxLayout(projectInfoView, projectActionsView, projectUsersView);
        layout.setFlexDirection(FlexDirection.COLUMN);
        return layout;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        MainLayout.get().getAppBar().setTitle("My Projects");

        var button = MainLayout.get().getAppBar().addActionItem(VaadinIcon.FILE_ADD, "New Project");
        button.addClickListener(event -> projectsAdminViewModel.createProject());

        projectsAdminViewModel.createProjectDialogs().subscribe(projectDialog -> {
            createProjectDialog.open(projectDialog.getProject(), projectDialog.getValidator());
        });

        projectSelectorViewModel.project().subscribe(projectRole -> {
            if (projectRole == null) {
                resetDetails();
            } else showProjectDetails(projectRole.getProject(), projectRole.getRole());
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        // TODO cancel subscriptions
    }
}
