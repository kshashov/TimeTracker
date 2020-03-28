package com.github.kshashov.timetracker.web.ui.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.web.ui.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.ui.components.ListItem;
import com.github.kshashov.timetracker.web.ui.layout.size.Vertical;
import com.github.kshashov.timetracker.web.ui.util.css.FlexDirection;
import com.github.kshashov.timetracker.web.ui.views.admin.projects.dialogs.ProjectEditorDialog;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import rx.Subscription;

import java.util.ArrayList;
import java.util.List;

@Scope("prototype")
@SpringComponent
public class ProjectInfoView extends FlexBoxLayout {
    private final ProjectInfoViewModel viewModel;
    private List<Subscription> subscriptions = new ArrayList<>();

    private final ProjectEditorDialog updateProjectDialog = new ProjectEditorDialog("Edit Project");
    private final ListItem projectListItem = new ListItem("");

    private Project project;

    @Autowired
    public ProjectInfoView(ProjectInfoViewModel viewModel) {
        this.viewModel = viewModel;

        initProjectLayout();

        setFlexDirection(FlexDirection.COLUMN);
        setSpacing(Vertical.S);
    }

    public void setProject(Project project, Role role) {
        viewModel.setProject(project, role);
    }

    private void initProjectLayout() {
        add(projectListItem);
    }

    private void showProject(Project project) {
        projectListItem.setPrimaryText(project.getTitle());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        subscriptions.add(viewModel.hasAccess()
                .subscribe(b -> {
                    setVisible(true);   // Readonly mode is always available
                }));

        subscriptions.add(viewModel.project()
                .subscribe(project -> {
                    this.project = project;
                    showProject(project);
                }));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        subscriptions.forEach(Subscription::unsubscribe);
        subscriptions.clear();
    }
}
