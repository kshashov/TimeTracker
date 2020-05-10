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
import com.github.kshashov.timetracker.data.service.admin.roles.AuthorizedProjectUsersService;
import com.github.kshashov.timetracker.data.service.admin.roles.ProjectRoleInfo;
import com.github.kshashov.timetracker.data.utils.OffsetLimitRequest;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import com.github.kshashov.timetracker.web.security.HasUser;
import com.github.kshashov.timetracker.web.ui.util.CrudEntity;
import com.github.kshashov.timetracker.web.ui.util.DataHandler;
import com.google.common.eventbus.EventBus;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.spring.annotation.SpringComponent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SpringComponent
public class ProjectUsersViewModel implements HasUser, DataHandler {
    private final EventBus eventBus;
    private final AuthorizedProjectUsersService projectRolesService;
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
            EventBus eventBus,
            AuthorizedProjectUsersService projectRolesService,
            ProjectRolesRepository projectRolesRepository,
            RolesRepository rolesRepository,
            UsersRepository usersRepository,
            RolePermissionsHelper rolePermissionsHelper) {
        this.eventBus = eventBus;
        this.rolePermissionsHelper = rolePermissionsHelper;
        this.user = getUser();
        this.projectRolesService = projectRolesService;
        this.projectRolesRepository = projectRolesRepository;

        // Except inactive
        this.roles = rolesRepository.findAll(Sort.by(Sort.Direction.ASC, "title")).stream()
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
                        () -> {
                            ProjectRoleInfo roleInfo = new ProjectRoleInfo(bean);
                            return projectRolesService.createProjectRole(user, bean.getProject().getId(), bean.getUser().getId(), roleInfo);
                        },
                        result -> reloadUsers()),
                roles,
                usersDataProvider
        ));
    }

    public void updateProjectRole(ProjectRole projectRole) {
        updateProjectRoleObservable.onNext(new ProjectUsersViewModel.ProjectRoleDialog(
                projectRole,
                bean -> handleDataManipulation(
                        () -> {
                            ProjectRoleInfo roleInfo = new ProjectRoleInfo(bean);
                            return projectRolesService.updateProjectRole(user, bean.getIdentity(), roleInfo);
                        },
                        result -> reloadUsers()),
                roles,
                usersDataProvider
        ));
    }

    public void deleteProjectRole(ProjectRole projectRole) {
        handleDataManipulation(
                () -> projectRolesService.deleteOrDeactivateProjectRole(user, projectRole.getIdentity()),
                result -> {
                    if (!result) {
                        notifyPopup("The user role is moved into inactive state instead of deletion, because there are closed working logs related to him.");
                    }
                    reloadUsers();
                });
    }

    public void reloadUsers() {
        if (access.equals(CrudEntity.CrudAccess.DENIED)) {
            projectRolesObservable.onNext(new CrudEntity<>(null, access));
            return;
        }

        List<ProjectRole> users = projectRolesRepository.findWithUserByProjectOrderByUserName(project);
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
    public static class ProjectRoleDialog {
        private final ProjectRole projectRole;
        private final Function<ProjectRole, ValidationResult> validator;
        private final List<Role> roles;
        private final CallbackDataProvider<User, String> usersDataProvider;
    }
}
