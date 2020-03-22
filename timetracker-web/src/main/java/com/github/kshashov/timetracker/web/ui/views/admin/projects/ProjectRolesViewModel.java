package com.github.kshashov.timetracker.web.ui.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.repo.user.ProjectRolesRepository;
import com.github.kshashov.timetracker.data.repo.user.RolesRepository;
import com.github.kshashov.timetracker.data.repo.user.UsersRepository;
import com.github.kshashov.timetracker.data.service.admin.projects.ProjectUsersServiceImpl;
import com.github.kshashov.timetracker.data.utils.OffsetLimitRequest;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import com.github.kshashov.timetracker.web.security.ProjectPermission;
import com.github.kshashov.timetracker.web.security.SecurityUtils;
import com.github.kshashov.timetracker.web.ui.util.DataHandler;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.spring.annotation.SpringComponent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import rx.Observable;
import rx.subjects.BehaviorSubject;

import java.util.List;
import java.util.function.Function;

@Scope("prototype")
@SpringComponent
public class ProjectRolesViewModel extends VerticalLayout implements DataHandler {
    private final ProjectUsersServiceImpl projectsAdminService;
    private final ProjectRolesRepository projectRolesRepository;
    private final UsersRepository usersRepository;
    private final RolePermissionsHelper rolePermissionsHelper;
    private final List<Role> roles;

    private final User user;
    private Project project;

    private final CallbackDataProvider<User, String> usersDataProvider;
    private final BehaviorSubject<List<ProjectRole>> projectRolesObservable = BehaviorSubject.create();
    private final BehaviorSubject<ProjectRoleDialog> createProjectRoleObservable = BehaviorSubject.create();
    private final BehaviorSubject<ProjectRoleDialog> updateProjectRoleObservable = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> hasAccessObservable = BehaviorSubject.create();

    @Autowired
    public ProjectRolesViewModel(
            ProjectUsersServiceImpl projectsAdminService,
            ProjectRolesRepository projectRolesRepository,
            RolesRepository rolesRepository,
            UsersRepository usersRepository,
            RolePermissionsHelper rolePermissionsHelper) {
        this.rolePermissionsHelper = rolePermissionsHelper;
        this.user = SecurityUtils.getCurrentUser().getUser();
        this.projectsAdminService = projectsAdminService;
        this.projectRolesRepository = projectRolesRepository;
        this.usersRepository = usersRepository;
        // TODO except inactive?
        this.roles = rolesRepository.findAll();
        this.usersDataProvider = new CallbackDataProvider<>(
                query -> {
                    var pageable = new OffsetLimitRequest(query.getOffset(), query.getLimit());
                    return usersRepository.findMissingProjectUsers(project.getId(), query.getFilter().orElse(""), pageable).getContent().stream();
                },
                query -> {
                    var pageable = new OffsetLimitRequest(query.getOffset(), query.getLimit());
                    return usersRepository.findMissingProjectUsers(project.getId(), query.getFilter().orElse(""), pageable).getNumberOfElements();
                });
    }

    public Observable<Boolean> hasAccess() {
        return hasAccessObservable;
    }

    public void setProject(Project project, Role role) {
        this.project = project;

        checkAccess(role);
        reloadUsers();
    }

    public void createProjectRole() {
        var projectRole = new ProjectRole();
        projectRole.setProject(project);

        createProjectRoleObservable.onNext(new ProjectRolesViewModel.ProjectRoleDialog(
                projectRole,
                bean -> handleDataManipulation(
                        () -> projectsAdminService.createProjectRole(user, bean),
                        result -> reloadUsers()),
                roles,
                usersDataProvider
        ));
    }

    public void updateProjectRole(ProjectRole projectRole) {
        updateProjectRoleObservable.onNext(new ProjectRolesViewModel.ProjectRoleDialog(
                projectRole,
                bean -> handleDataManipulation(
                        () -> projectsAdminService.updateProjectRole(user, bean),
                        result -> reloadUsers()),
                roles,
                usersDataProvider
        ));
    }

    public Observable<List<ProjectRole>> projectRoles() {
        return projectRolesObservable;
    }

    public Observable<ProjectRoleDialog> createRoleDialogs() {
        return createProjectRoleObservable;
    }

    public Observable<ProjectRoleDialog> updateRoleDialogs() {
        return updateProjectRoleObservable;
    }

    private void checkAccess(Role role) {
        hasAccessObservable.onNext(rolePermissionsHelper.hasPermission(role, ProjectPermission.EDIT_PROJECT_USERS));
    }

    private void reloadUsers() {
        List<ProjectRole> users = projectRolesRepository.findProjectUsersWithRoles(project.getId());
        projectRolesObservable.onNext(users);
    }

    @Getter
    @AllArgsConstructor
    public static class ProjectRoleDialog {
        private final ProjectRole projectRole;
        private final Function<ProjectRole, ValidationResult> validator;
        private final List<Role> roles;
        private final CallbackDataProvider<User, String> usersDataProvider;
    }
}