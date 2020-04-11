package com.github.kshashov.timetracker.web.ui.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.web.ui.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.ui.components.ListItem;
import com.github.kshashov.timetracker.web.ui.layout.size.Vertical;
import com.github.kshashov.timetracker.web.ui.util.css.FlexDirection;
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

    private final ListItem title = new ListItem("Title");
    private final ListItem status = new ListItem("Status");

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
        title.setDividerVisible(true);
        status.setDividerVisible(false);
        add(title, status);
    }

    private void showProject(Project project) {
        title.setSecondaryText(project.getTitle());
        status.setSecondaryText(project.getIsActive() ? "Active" : "Inactive");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        subscriptions.add(viewModel.project()
                .subscribe(project -> {
                    if (project.getAccess().canView()) {
                        setVisible(true);
                        showProject(project.getEntity());
                    } else {
                        setVisible(false);
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
