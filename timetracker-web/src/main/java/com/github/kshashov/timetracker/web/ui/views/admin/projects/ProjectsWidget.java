package com.github.kshashov.timetracker.web.ui.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.web.security.HasUser;
import com.github.kshashov.timetracker.web.ui.components.RoleBadge;
import com.github.kshashov.timetracker.web.ui.components.Widget;
import com.github.kshashov.timetracker.web.ui.util.DataHandler;
import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.github.kshashov.timetracker.web.ui.views.admin.projects.dialogs.ProjectEditorDialog;
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
public class ProjectsWidget extends Widget implements HasUser, DataHandler {
    private final ProjectsViewModel viewModel;
    private final List<Subscription> subscriptions = new ArrayList<>();

    private final Grid<ProjectRole> projectsGrid = new Grid<>();
    private final ProjectEditorDialog createProjectDialog = new ProjectEditorDialog("Create Project");
    private final ProjectEditorDialog editProjectDialog = new ProjectEditorDialog("Edit Project");

    @Autowired
    public ProjectsWidget(ProjectsViewModel viewModel) {
        this.viewModel = viewModel;

        initProjectsGrid();
        initActions();

        setSizeFull();
        setTitle("Projects");
    }

    private void initActions() {
        Button plus = UIUtils.createTertiaryButton(VaadinIcon.PLUS_CIRCLE_O);
        plus.addClickListener(event -> viewModel.createProject());

        Button refresh = UIUtils.createTertiaryButton(VaadinIcon.REFRESH);
        refresh.addClickListener(event -> viewModel.reloadProjects());

        addActions(plus, refresh);
    }

    private void initProjectsGrid() {
        projectsGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        projectsGrid.setSizeFull();

        projectsGrid.addColumn(new ComponentRenderer<>(pr -> {
            var span = new Span(pr.getProject().getTitle());
            return span;
        })).setHeader("Project").setSortable(true).setComparator(Comparator.comparing(o -> o.getProject().getTitle())).setAutoWidth(true);
        projectsGrid.addColumn(new ComponentRenderer<>(pr -> {
            return new RoleBadge(pr.getRole());
        })).setHeader("Role").setComparator(Comparator.comparing(o -> o.getRole().getCode())).setSortable(false).setAutoWidth(true).setFlexGrow(0);
        projectsGrid.addColumn(new ComponentRenderer<>(pr -> {
            var layout = new HorizontalLayout();

            var edit = UIUtils.createActionButton(VaadinIcon.PENCIL);
            edit.addClickListener(e -> viewModel.updateProject(pr.getProject()));
            var delete = UIUtils.createActionButton(VaadinIcon.FILE_REMOVE);
            delete.addClickListener(e -> viewModel.updateProject(pr.getProject()));

            layout.add(edit);
            layout.add(delete);
            return layout;
        })).setHeader("");
        projectsGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
//        ((GridSingleSelectionModel<ProjectRoles>) projectsGrid.getSelectionModel()).setDeselectAllowed(false);
        projectsGrid.addSelectionListener(selectionEvent -> {
            selectionEvent.getFirstSelectedItem().ifPresent(viewModel::select);
        });

        addContentItems(projectsGrid);
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

        subscriptions.add(viewModel.projects()
                .subscribe(this::reloadProjects));

        subscriptions.add(viewModel.createProjectDialogs()
                .subscribe(projectDialog -> {
                    createProjectDialog.open(projectDialog.getProject(), projectDialog.getValidator());
                }));

        subscriptions.add(viewModel.updateProjectDialogs()
                .subscribe(projectDialog -> {
                    editProjectDialog.open(projectDialog.getProject(), projectDialog.getValidator());
                }));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        subscriptions.forEach(Subscription::unsubscribe);
        subscriptions.clear();
    }
}
