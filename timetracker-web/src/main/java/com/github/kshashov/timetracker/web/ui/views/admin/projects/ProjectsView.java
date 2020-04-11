package com.github.kshashov.timetracker.web.ui.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.web.ui.MainLayout;
import com.github.kshashov.timetracker.web.ui.components.ConfirmDialog;
import com.github.kshashov.timetracker.web.ui.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.ui.components.detail.Detail;
import com.github.kshashov.timetracker.web.ui.components.detail.DetailHeader;
import com.github.kshashov.timetracker.web.ui.layout.size.Uniform;
import com.github.kshashov.timetracker.web.ui.layout.size.Vertical;
import com.github.kshashov.timetracker.web.ui.util.CrudEntity;
import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.github.kshashov.timetracker.web.ui.util.css.BoxSizing;
import com.github.kshashov.timetracker.web.ui.util.css.FlexDirection;
import com.github.kshashov.timetracker.web.ui.views.MasterDetail;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import rx.Subscription;

import java.util.ArrayList;
import java.util.List;

@Route(value = "projects", layout = MainLayout.class)
@PageTitle("Projects")
public class ProjectsView extends MasterDetail {
    private final ProjectsViewModel projectsViewModel;
    private final List<Subscription> subscriptions = new ArrayList<>();

    private Project selectedProject;

    private final ProjectsWidget userProjectsView;
    private final ProjectActionsWidget projectActionsView;
    private final ProjectInfoView projectInfoView;
    private final ProjectUsersWidget projectUsersView;
    private final Detail detailsDrawer = getDetailsDrawer();
    private final DetailHeader detailsDrawerHeader = new DetailHeader("");
    private final ConfirmDialog activateDialog;
    private final ConfirmDialog deleteDialog;
    private final Button edit = UIUtils.createActionButton(VaadinIcon.PENCIL);
    private final Button delete = UIUtils.createActionButton(VaadinIcon.FILE_REMOVE);
    private final Button activate = UIUtils.createActionButton(VaadinIcon.PLUS);

    @Autowired
    public ProjectsView(
            ProjectsViewModel projectsViewModel,
            ProjectsWidget userProjectsView,
            ProjectActionsWidget projectActionsView,
            ProjectUsersWidget projectUsersView,
            ProjectInfoView projectInfoView) {
        super(Position.RIGHT);
        this.projectsViewModel = projectsViewModel;

        this.userProjectsView = userProjectsView;
        this.projectInfoView = projectInfoView;
        this.projectActionsView = projectActionsView;
        this.projectUsersView = projectUsersView;

        deleteDialog = new ConfirmDialog("Delete", "Are you sure you want to delete this project?",
                b -> {
                    if (b) projectsViewModel.deleteProject(selectedProject);
                });

        activateDialog = new ConfirmDialog("Enable", "Are you sure you want to enable this project?",
                b -> {
                    if (b) projectsViewModel.activateProject(selectedProject);
                });

        setDetailSize(Size.LARGE);
        setViewContent(createContent());
        initDetailsDrawer();
    }

    private Component createContent() {
        FlexBoxLayout content = new FlexBoxLayout(userProjectsView);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setFlexDirection(FlexDirection.COLUMN);
        content.setHeightFull();
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

        projectInfoView.setProject(project, role);
        projectActionsView.setProject(project, role);
        projectUsersView.setProject(project, role);

        detailsDrawer.expand();
    }

    private void initDetailsDrawer() {
        // Header
        edit.addClickListener(e -> projectsViewModel.updateProject(selectedProject));
        delete.addClickListener(e -> deleteDialog.open());
        activate.addClickListener(e -> activateDialog.open());

        detailsDrawerHeader.setCanReset(false);
        detailsDrawerHeader.addActions(edit, delete, activate);
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
        layout.setPadding(Uniform.M);
        layout.setSpacing(Vertical.S);
        return layout;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        MainLayout.get().getAppBar().setTitle("My Projects");

        subscriptions.add(projectsViewModel.project()
                .subscribe(projectRole -> {

                    if (projectRole.getEntity() == null) {
                        selectedProject = null;
                        edit.setVisible(false);
                        delete.setVisible(false);
                        activate.setVisible(false);
                        resetDetails();
                    } else {
                        selectedProject = projectRole.getEntity().getProject();
                        CrudEntity.CrudAccess access = projectRole.getAccess();
                        edit.setVisible(access.canEdit() && selectedProject.getIsActive());
                        delete.setVisible(access.canDelete() && selectedProject.getIsActive());
                        activate.setVisible(access.canEnable() && !selectedProject.getIsActive());
                        showProjectDetails(projectRole.getEntity().getProject(), projectRole.getEntity().getRole());
                    }
                }));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        subscriptions.forEach(Subscription::unsubscribe);
        subscriptions.clear();
    }
}
