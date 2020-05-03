package com.github.kshashov.timetracker.web.ui.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.repo.ActionsRepository;
import com.github.kshashov.timetracker.data.service.admin.projects.ProjectActionsService;
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
public class ProjectActionsViewModel implements HasUser, DataHandler {
    private final EventBus eventBus;
    private final ActionsRepository actionsRepository;
    private final ProjectActionsService actionsService;
    private final RolePermissionsHelper rolePermissionsHelper;

    private Project project;
    private final User user;
    private CrudEntity.CrudAccess access;

    private final PublishSubject<ActionDialog> createActionDialogObservable = PublishSubject.create();
    private final PublishSubject<ActionDialog> updateActionDialogObservable = PublishSubject.create();
    private final BehaviorSubject<CrudEntity<List<Action>>> actionsObservable = BehaviorSubject.create();

    @Autowired
    public ProjectActionsViewModel(
            EventBus eventBus,
            ActionsRepository actionsRepository,
            ProjectActionsService actionsService,
            RolePermissionsHelper rolePermissionsHelper) {
        this.eventBus = eventBus;
        this.rolePermissionsHelper = rolePermissionsHelper;
        this.user = getUser();
        this.actionsRepository = actionsRepository;
        this.actionsService = actionsService;
    }

    public void setProject(Project project, Role role) {
        this.project = project;
        this.access = checkAccess(project, role);

        reloadActions();
    }

    public void createAction() {
        var action = new Action();
        action.setProject(project);
        action.setIsActive(true);

        createActionDialogObservable.onNext(new ActionDialog(
                action,
                bean -> handleDataManipulation(
                        () -> actionsService.createAction(user, bean),
                        result -> reloadActions())
        ));
    }

    public void updateAction(Action action) {
        updateActionDialogObservable.onNext(new ActionDialog(
                action,
                bean -> handleDataManipulation(
                        () -> actionsService.updateAction(user, bean),
                        result -> reloadActions())
        ));
    }

    public void activateAction(Action action) {
        handleDataManipulation(
                () -> actionsService.activateAction(action.getId()),
                () -> reloadActions());
    }

    public void deleteAction(Action action) {
        handleDataManipulation(
                () -> actionsService.deleteOrDeactivateAction(user, action.getId()),
                result -> {
                    if (!result) {
                        notifyPopup("The action is moved into inactive state instead of deletion, because there are closed working logs related to it.");
                    }
                    reloadActions();
                });
    }

    public Observable<CrudEntity<List<Action>>> actions() {
        return actionsObservable;
    }

    public Observable<ActionDialog> createActionDialogs() {
        return createActionDialogObservable;
    }

    public Observable<ActionDialog> updateActionDialogs() {
        return updateActionDialogObservable;
    }

    private CrudEntity.CrudAccess checkAccess(Project project, Role role) {
        if (rolePermissionsHelper.hasPermission(role, ProjectPermissionType.EDIT_PROJECT_ACTIONS)) {
            if (!project.getIsActive()) {
                return CrudEntity.CrudAccess.READ_ONLY;
            }
            return CrudEntity.CrudAccess.FULL_ACCESS;
        } else if (rolePermissionsHelper.hasPermission(role, ProjectPermissionType.VIEW_PROJECT_INFO)) {
            return CrudEntity.CrudAccess.READ_ONLY;
        }
        return CrudEntity.CrudAccess.DENIED;
    }

    public void reloadActions() {
        if (access.equals(CrudEntity.CrudAccess.DENIED)) {
            actionsObservable.onNext(new CrudEntity<>(null, access));
            return;
        }

        List<Action> actions = actionsRepository.findWithProjectByProject(project);
        actionsObservable.onNext(new CrudEntity<>(actions, access));
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
    public static class ActionDialog {
        private final Action action;
        private final Function<Action, ValidationResult> validator;
    }
}
