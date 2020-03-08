package com.github.kshashov.timetracker.web.ui.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.repo.user.ProjectRolesRepository;
import com.github.kshashov.timetracker.web.security.HasUser;
import com.github.kshashov.timetracker.web.ui.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.ui.util.DataHandler;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Comparator;

@Scope("prototype")
@SpringComponent
public class UserProjectsView extends FlexBoxLayout implements HasUser, DataHandler {
    private final ProjectRolesRepository projectRolesRepository;

    private final User user;
    private final Grid<ProjectRole> projectsGrid = new Grid<>();
    private final ListDataProvider<ProjectRole> projectsDataProvider = new ListDataProvider<>(new ArrayList<>());
    private Project selectedProject;

    @Autowired
    public UserProjectsView(ProjectRolesRepository projectRolesRepository) {
        this.projectRolesRepository = projectRolesRepository;
        this.user = getUser();

        add(initProjectsGrid());
        setSizeFull();
    }

    private Grid<ProjectRole> initProjectsGrid() {
        projectsGrid.addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS);
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
//        ((GridSingleSelectionModel<ProjectRoles>) projectsGrid.getSelectionModel()).setDeselectAllowed(false);
        projectsGrid.addSelectionListener(selectionEvent -> selectionEvent.getFirstSelectedItem().ifPresent(
                projectRole -> {
                    selectedProject = projectRole.getProject();
                    fireEvent(new ProjectSelectedEvent(this, projectRole.getProject(), projectRole.getRole()));
                }
        ));


        projectsGrid.setDataProvider(projectsDataProvider);
        return projectsGrid;
    }

    public void reloadProjects() {
        // Reload items
        var rows = projectRolesRepository.findUserProjectsWithRoles(user.getId());
        projectsDataProvider.getItems().clear();
        projectsDataProvider.getItems().addAll(rows);
        projectsDataProvider.refreshAll();

        if (selectedProject != null) {
            // Restore selection
            rows.stream().filter(pr -> pr.getProject().getId().equals(selectedProject.getId()))
                    .findFirst()
                    .ifPresentOrElse(projectsGrid::select, () -> selectedProject = null);
        }

        if ((selectedProject == null) && (rows.size() > 0)) {
            // Select first item
            Notification.show("select " + rows.iterator().next().getProject().getTitle());
            projectsGrid.select(rows.iterator().next());
        }
    }

    public void deselectAll() {
        selectedProject = null;
        projectsGrid.deselectAll();
    }

    public Registration addOnProjectSelectedEventListener(@NotNull ComponentEventListener<UserProjectsView.ProjectSelectedEvent> listener) {
        return addListener(ProjectSelectedEvent.class, listener);
    }

    @Getter
    public static class ProjectSelectedEvent extends ComponentEvent<UserProjectsView> {
        private final Project project;
        private final Role role;

        public ProjectSelectedEvent(UserProjectsView source, Project project, Role role) {
            super(source, false);
            this.project = project;
            this.role = role;
        }
    }
}
