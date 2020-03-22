package com.github.kshashov.timetracker.web.ui.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.repo.ActionsRepository;
import com.github.kshashov.timetracker.data.service.admin.projects.ProjectActionsService;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import com.github.kshashov.timetracker.web.security.ProjectPermission;
import com.github.kshashov.timetracker.web.security.SecurityUtils;
import com.github.kshashov.timetracker.web.ui.util.DataHandler;
import com.vaadin.flow.data.binder.ValidationResult;
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
public class ProjectActionsViewModel implements DataHandler {
    private final ActionsRepository actionsRepository;
    private final ProjectActionsService actionsService;
    private final RolePermissionsHelper rolePermissionsHelper;

    private Project project;
    private final User user;

    private final BehaviorSubject<ActionDialog> createActionDialogObservable = BehaviorSubject.create();
    private final BehaviorSubject<ActionDialog> updateActionDialogObservable = BehaviorSubject.create();
    private final BehaviorSubject<List<Action>> actionsObservable = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> hasAccessObservable = BehaviorSubject.create();

    @Autowired
    public ProjectActionsViewModel(ActionsRepository actionsRepository, ProjectActionsService actionsService, RolePermissionsHelper rolePermissionsHelper) {
        this.rolePermissionsHelper = rolePermissionsHelper;
        this.user = SecurityUtils.getCurrentUser().getUser();
        this.actionsRepository = actionsRepository;
        this.actionsService = actionsService;
    }

    public void setProject(Project project, Role role) {
        this.project = project;

        if (checkAccess(role)) {
            reloadActions();
        }
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

    public Observable<Boolean> hasAccess() {
        return hasAccessObservable;
    }

    public Observable<List<Action>> actions() {
        return actionsObservable;
    }

    public Observable<ActionDialog> createActionDialogs() {
        return createActionDialogObservable;
    }

    public Observable<ActionDialog> updateActionDialogs() {
        return updateActionDialogObservable;
    }

    private boolean checkAccess(Role role) {
        boolean hasAccess = rolePermissionsHelper.hasPermission(role, ProjectPermission.EDIT_PROJECT_ACTIONS);
        hasAccessObservable.onNext(hasAccess);
        return hasAccess;
    }

    private void reloadActions() {
        List<Action> actions = actionsRepository.findAllByProject(project.getId());
        actionsObservable.onNext(actions);
    }

    @Getter
    @AllArgsConstructor
    public static class ActionDialog {
        private final Action action;
        private final Function<Action, ValidationResult> validator;
    }
}
