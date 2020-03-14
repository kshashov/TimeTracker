package com.github.kshashov.timetracker.web.ui.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.web.security.HasUser;
import com.github.kshashov.timetracker.web.ui.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.ui.components.RoleBadge;
import com.github.kshashov.timetracker.web.ui.util.DataHandler;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
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
public class UserProjectsView extends FlexBoxLayout implements HasUser, DataHandler {
    private final ProjectSelectorViewModel projectSelectorViewModel;
    private final List<Subscription> subscriptions = new ArrayList<>();

    private final Grid<ProjectRole> projectsGrid = new Grid<>();

    @Autowired
    public UserProjectsView(ProjectSelectorViewModel projectSelectorViewModel) {
        this.projectSelectorViewModel = projectSelectorViewModel;

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
            return new RoleBadge(pr.getRole());
        })).setHeader("Role").setComparator(Comparator.comparing(o -> o.getRole().getCode())).setSortable(false).setAutoWidth(true).setFlexGrow(0);

        projectsGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
//        ((GridSingleSelectionModel<ProjectRoles>) projectsGrid.getSelectionModel()).setDeselectAllowed(false);
        projectsGrid.addSelectionListener(selectionEvent -> selectionEvent.getFirstSelectedItem().ifPresent(projectSelectorViewModel::select));

        return projectsGrid;
    }

    public void deselectAll() {
        projectsGrid.deselectAll();
    }

    private void reloadProjects(List<ProjectRole> projectRoles) {
        projectsGrid.setItems(projectRoles);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        subscriptions.add(projectSelectorViewModel.projects()
                .subscribe(this::reloadProjects));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        subscriptions.forEach(Subscription::unsubscribe);
        subscriptions.clear();
    }
}
