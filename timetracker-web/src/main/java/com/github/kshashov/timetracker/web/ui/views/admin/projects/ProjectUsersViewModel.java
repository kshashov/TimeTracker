package com.github.kshashov.timetracker.web.ui.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.enums.ProjectRoleType;
import com.github.kshashov.timetracker.data.repo.user.ProjectRolesRepository;
import com.github.kshashov.timetracker.data.repo.user.RolesRepository;
import com.github.kshashov.timetracker.data.repo.user.UsersRepository;
import com.github.kshashov.timetracker.data.service.admin.projects.ProjectUsersServiceImpl;
import com.github.kshashov.timetracker.data.utils.OffsetLimitRequest;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import com.github.kshashov.timetracker.web.security.HasUser;
import com.github.kshashov.timetracker.web.ui.util.CrudEntity;
import com.github.kshashov.timetracker.web.ui.util.DataHandler;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.provider.CallbackDataProvider;
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
import java.util.stream.Collectors;

@UIScope
@SpringComponent
public class ProjectUsersViewModel extends VerticalLayout implements HasUser, DataHandler {
    private final ProjectUsersServiceImpl projectRolesService;
    private final ProjectRolesRepository projectRolesRepository;
    private final RolePermissionsHelper rolePermissionsHelper;
    private final List<Role> roles;

    private final User user;
    private Project project;

    private final CallbackDataProvider<User, String> usersDataProvider;
    private CrudEntity.CrudAccess access;

    private final PublishSubject<ProjectRoleDialog> createProjectRoleObservable = PublishSubject.create();
    private final PublishSubject<ProjectRoleDialog> updateProjectRoleObservable = PublishSubject.create();
    private final BehaviorSubject<CrudEntity<List<ProjectRole>>> projectRolesObservable = BehaviorSubject.create();

    @Autowired
    public ProjectUsersViewModel(
            ProjectUsersServiceImpl projectRolesService,
            ProjectRolesRepository projectRolesRepository,
            RolesRepository rolesRepository,
            UsersRepository usersRepository,
            RolePermissionsHelper rolePermissionsHelper) {
        this.rolePermissionsHelper = rolePermissionsHelper;
        this.user = getUser();
        this.projectRolesService = projectRolesService;
        this.projectRolesRepository = projectRolesRepository;

        // Except inactive
        this.roles = rolesRepository.findAll().stream()
                .filter(r -> !ProjectRoleType.isInactive(r))
                .collect(Collectors.toList());

        this.usersDataProvider = new CallbackDataProvider<>(
                query -> {
                    var pageable = new OffsetLimitRequest(query.getOffset(), query.getLimit());
                    return usersRepository.findMissingProjectUsers(project, query.getFilter().orElse(""), pageable).getContent().stream();
                },
                query -> {
                    var pageable = new OffsetLimitRequest(query.getOffset(), query.getLimit());
                    return usersRepository.findMissingProjectUsers(project, query.getFilter().orElse(""), pageable).getNumberOfElements();
                });
    }

    public void setProject(Project project, Role role) {
        this.project = project;
        this.access = checkAccess(project, role);

        reloadUsers();
    }

    public void createProjectRole() {
        var projectRole = new ProjectRole();
        projectRole.setProject(project);

        createProjectRoleObservable.onNext(new ProjectUsersViewModel.ProjectRoleDialog(
                projectRole,
                bean -> handleDataManipulation(
                        () -> projectRolesService.createProjectRole(user, bean),
                        result -> reloadUsers()),
                roles,
                usersDataProvider
        ));
    }

    public void updateProjectRole(ProjectRole projectRole) {
        updateProjectRoleObservable.onNext(new ProjectUsersViewModel.ProjectRoleDialog(
                projectRole,
                bean -> handleDataManipulation(
                        () -> projectRolesService.updateProjectRole(user, bean),
                        result -> reloadUsers()),
                roles,
                usersDataProvider
        ));
    }

    public void deleteProjectRole(ProjectRole projectRole) {
        handleDataManipulation(
                () -> projectRolesService.deleteOrDeactivateProjectRole(projectRole.getIdentity()),
                result -> reloadUsers());
    }

    public void reloadUsers() {
        if (access.equals(CrudEntity.CrudAccess.DENIED)) {
            projectRolesObservable.onNext(new CrudEntity<>(null, access));
            return;
        }

        List<ProjectRole> users = projectRolesRepository.findWithUserByProject(project);
        projectRolesObservable.onNext(new CrudEntity<>(users, access));
    }

    public Observable<CrudEntity<List<ProjectRole>>> projectRoles() {
        return projectRolesObservable;
    }

    public Observable<ProjectRoleDialog> createRoleDialogs() {
        return createProjectRoleObservable;
    }

    public Observable<ProjectRoleDialog> updateRoleDialogs() {
        return updateProjectRoleObservable;
    }

    private CrudEntity.CrudAccess checkAccess(Project project, Role role) {
        if (rolePermissionsHelper.hasPermission(role, ProjectPermissionType.EDIT_PROJECT_USERS)) {
            if (!project.getIsActive()) {
                return CrudEntity.CrudAccess.READ_ONLY;
            }
            return CrudEntity.CrudAccess.FULL_ACCESS;
        } else if (rolePermissionsHelper.hasPermission(role, ProjectPermissionType.VIEW_PROJECT_INFO)) {
            return CrudEntity.CrudAccess.READ_ONLY;
        }

        return CrudEntity.CrudAccess.DENIED;
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
