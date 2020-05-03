package com.github.kshashov.timetracker.web.ui.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.repo.user.ProjectRolesRepository;
import com.github.kshashov.timetracker.data.service.admin.projects.ProjectsService;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import com.github.kshashov.timetracker.web.security.HasUser;
import com.github.kshashov.timetracker.web.ui.util.CrudEntity;
import com.github.kshashov.timetracker.web.ui.util.DataHandler;
import com.google.common.eventbus.EventBus;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.spring.annotation.SpringComponent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import java.util.List;
import java.util.function.Function;

@Slf4j
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SpringComponent
public class ProjectsViewModel implements HasUser, DataHandler {
    private final EventBus eventBus;
    private final ProjectsService projectsService;
    private final ProjectRolesRepository projectRolesRepository;
    private final RolePermissionsHelper rolePermissionsHelper;

    private final PublishSubject<ProjectDialog> createProjectDialogObservable = PublishSubject.create();
    private final PublishSubject<ProjectDialog> updateProjectDialogObservable = PublishSubject.create();
    private final BehaviorSubject<CrudEntity<ProjectRole>> selectedProjectObservable = BehaviorSubject.create();
    private final BehaviorSubject<CrudEntity<List<ProjectRole>>> projectsObservable = BehaviorSubject.create();

    private final User user;
    private ProjectRole selectedProject;

    @Autowired
    public ProjectsViewModel(
            EventBus eventBus,
            ProjectsService projectsService,
            ProjectRolesRepository projectRolesRepository,
            RolePermissionsHelper rolePermissionsHelper) {
        this.eventBus = eventBus;
        this.projectsService = projectsService;
        this.projectRolesRepository = projectRolesRepository;
        this.rolePermissionsHelper = rolePermissionsHelper;
        this.user = getUser();

        select(null);
        reloadProjects();
    }

    public void select(ProjectRole projectRole) {
        CrudEntity.CrudAccess projectAccess = projectRole == null
                ? CrudEntity.CrudAccess.READ_ONLY
                : checkAccess(projectRole.getRole());

        selectedProject = projectRole;
        selectedProjectObservable.onNext(new CrudEntity<>(projectRole, projectAccess));
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

    public void activateProject(Project project) {
        handleDataManipulation(
                () -> projectsService.activateProject(project.getId()),
                () -> reloadProjects());
    }

    public void deleteProject(Project project) {
        handleDataManipulation(
                () -> projectsService.deleteOrDeactivateProject(project.getId()),
                result -> {
                    if (!result) {
                        notifyPopup("The project with some actions are moved into inactive state instead of deletion, because there are closed working logs related to it.");
                    }
                    reloadProjects();
                });
    }

    public void reloadProjects() {
        var projectRoles = projectRolesRepository.findWithProjectByUser(user);

        if (selectedProject != null) {
            // Restore selection
            projectRoles.stream().filter(pr -> pr.getProject().getId().equals(selectedProject.getProject().getId()))
                    .findFirst()
                    .ifPresentOrElse(this::select, () -> selectedProject = null);
        }

        if ((selectedProject == null) && (projectRoles.size() > 0)) {
            // Select first item
            select(projectRoles.iterator().next());
        } else if (selectedProject == null) {
            select(null);
        }

        projectsObservable.onNext(new CrudEntity<>(projectRoles, CrudEntity.CrudAccess.FULL_ACCESS));
    }

    public Observable<CrudEntity<ProjectRole>> project() {
        return selectedProjectObservable;
    }

    public Observable<CrudEntity<List<ProjectRole>>> projects() {
        return projectsObservable;
    }

    public Observable<ProjectDialog> createProjectDialogs() {
        return createProjectDialogObservable;
    }

    public Observable<ProjectsViewModel.ProjectDialog> updateProjectDialogs() {
        return updateProjectDialogObservable;
    }

    private CrudEntity.CrudAccess checkAccess(Role role) {
        if (rolePermissionsHelper.hasPermission(role, ProjectPermissionType.EDIT_PROJECT_INFO)) {
            return CrudEntity.CrudAccess.FULL_ACCESS;
        } else if (rolePermissionsHelper.hasPermission(role, ProjectPermissionType.VIEW_PROJECT_INFO)) {
            return CrudEntity.CrudAccess.READ_ONLY;
        }

        return CrudEntity.CrudAccess.DENIED;
    }

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public EventBus eventBus() {
        return eventBus;
    }

    @Getter
    @AllArgsConstructor
    public static class ProjectDialog {

        private final Project project;
        private final Function<Project, ValidationResult> validator;
    }
}
