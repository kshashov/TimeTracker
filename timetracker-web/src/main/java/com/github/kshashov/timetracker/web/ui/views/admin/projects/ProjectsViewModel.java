package com.github.kshashov.timetracker.web.ui.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.repo.user.ProjectRolesRepository;
import com.github.kshashov.timetracker.data.service.admin.projects.ProjectsService;
import com.github.kshashov.timetracker.web.security.HasUser;
import com.github.kshashov.timetracker.web.ui.util.DataHandler;
import com.google.common.eventbus.EventBus;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import java.util.List;
import java.util.function.Function;

@UIScope
@SpringComponent
public class ProjectsViewModel implements HasUser, DataHandler {
    private final ProjectsService projectsService;
    private final ProjectRolesRepository projectRolesRepository;

    private final PublishSubject<ProjectDialog> createProjectDialogObservable = PublishSubject.create();
    private final PublishSubject<ProjectDialog> updateProjectDialogObservable = PublishSubject.create();
    private final BehaviorSubject<ProjectRole> selectedProjectObservable = BehaviorSubject.create();
    private final BehaviorSubject<List<ProjectRole>> projectsObservable = BehaviorSubject.create();

    private final User user;
    private ProjectRole selectedProject;

    @Autowired
    public ProjectsViewModel(ProjectsService projectsService, ProjectRolesRepository projectRolesRepository, EventBus eventBus, EventBus eventBus1) {
        this.projectsService = projectsService;
        this.projectRolesRepository = projectRolesRepository;
        this.user = getUser();

        select(null);
        reloadProjects();
    }

    public void select(ProjectRole projectRole) {
        selectedProject = projectRole;
        selectedProjectObservable.onNext(projectRole);
    }

    public void createProject() {
        var project = new Project();
        project.setIsActive(true);

        createProjectDialogObservable.onNext(new ProjectsViewModel.ProjectDialog(
                project,
                bean -> handleDataManipulation(
                        () -> projectsService.createProject(user, bean),
                        result -> reloadProjects())
        ));
    }

    public void updateProject(Project project) {
        updateProjectDialogObservable.onNext(new ProjectsViewModel.ProjectDialog(
                project,
                bean -> handleDataManipulation(
                        () -> projectsService.updateProject(user, bean),
                        result -> reloadProjects())
        ));
    }

    public void reloadProjects() {
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

    public Observable<ProjectRole> project() {
        return selectedProjectObservable;
    }

    public Observable<List<ProjectRole>> projects() {
        return projectsObservable;
    }

    public Observable<ProjectDialog> createProjectDialogs() {
        return createProjectDialogObservable;
    }

    public Observable<ProjectsViewModel.ProjectDialog> updateProjectDialogs() {
        return updateProjectDialogObservable;
    }

    @Getter
    @AllArgsConstructor
    public static class ProjectDialog {

        private final Project project;
        private final Function<Project, ValidationResult> validator;
    }
}
