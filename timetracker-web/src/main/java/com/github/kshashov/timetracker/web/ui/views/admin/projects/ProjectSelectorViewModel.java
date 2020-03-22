package com.github.kshashov.timetracker.web.ui.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.repo.user.ProjectRolesRepository;
import com.github.kshashov.timetracker.web.security.HasUser;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.context.annotation.Scope;
import rx.Observable;
import rx.subjects.BehaviorSubject;

import java.util.List;

@Scope("prototype")
@SpringComponent
public class ProjectSelectorViewModel implements HasUser {
    private final ProjectRolesRepository projectRolesRepository;

    private final BehaviorSubject<ProjectRole> selectedProjectObservable = BehaviorSubject.create();
    private final BehaviorSubject<List<ProjectRole>> projectsObservable = BehaviorSubject.create();

    private final User user;
    private ProjectRole selectedProject;

    public ProjectSelectorViewModel(ProjectRolesRepository projectRolesRepository, EventBus eventBus) {
        this.projectRolesRepository = projectRolesRepository;
        this.user = getUser();

        select(null);
        reloadProjects();

        eventBus.register(this);
    }

    @Subscribe
    public void onTestEvent(ReloadProjectsEvent e) {
        reloadProjects();
    }

    public void select(ProjectRole projectRole) {
        selectedProject = projectRole;
        selectedProjectObservable.onNext(projectRole);
    }


    public Observable<ProjectRole> project() {
        return selectedProjectObservable;
    }

    public Observable<List<ProjectRole>> projects() {
        return projectsObservable;
    }

    private void reloadProjects() {
        var projectRoles = projectRolesRepository.findUserProjectsWithRoles(user.getId());

        if (selectedProject != null) {
            // Restore selection
            projectRoles.stream().filter(pr -> pr.getProject().getId().equals(selectedProject.getProject().getId()))
                    .findFirst()
                    .ifPresentOrElse(this::select, () -> selectedProject = null);
        }

        if ((selectedProject == null) && (projectRoles.size() > 0)) {
            // Select first item
            select(projectRoles.iterator().next());
        }

        projectsObservable.onNext(projectRoles);
    }

    public static class ReloadProjectsEvent {
    }
}
