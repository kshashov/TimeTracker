package com.github.kshashov.timetracker.web.ui.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.service.admin.projects.ProjectsService;
import com.github.kshashov.timetracker.web.security.HasUser;
import com.github.kshashov.timetracker.web.ui.util.DataHandler;
import com.google.common.eventbus.EventBus;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.spring.annotation.SpringComponent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import rx.Observable;
import rx.subjects.BehaviorSubject;

import java.util.function.Function;

@Scope("prototype")
@SpringComponent
public class ProjectsViewModel implements HasUser, DataHandler {
    private final ProjectsService projectsService;

    private final BehaviorSubject<ProjectDialog> createProjectDialogObservable = BehaviorSubject.create();

    private final EventBus eventBus;
    private final User user;

    @Autowired
    public ProjectsViewModel(ProjectsService projectsService, EventBus eventBus) {
        this.projectsService = projectsService;
        this.eventBus = eventBus;
        this.user = getUser();
    }

    public void createProject() {
        var project = new Project();
        project.setIsActive(true);

        // TODO Fire reload event
        createProjectDialogObservable.onNext(new ProjectsViewModel.ProjectDialog(
                project,
                bean -> handleDataManipulation(
                        () -> projectsService.createProject(user, bean),
                        result -> eventBus.post(new ProjectSelectorViewModel.ReloadProjectsEvent()))
        ));
    }

    public Observable<ProjectDialog> createProjectDialogs() {
        return createProjectDialogObservable;
    }

    @Getter
    @AllArgsConstructor
    public static class ProjectDialog {
        private final Project project;
        private final Function<Project, ValidationResult> validator;
    }
}
