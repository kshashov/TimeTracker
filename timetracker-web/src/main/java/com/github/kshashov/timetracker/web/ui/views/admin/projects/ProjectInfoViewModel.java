package com.github.kshashov.timetracker.web.ui.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import com.github.kshashov.timetracker.web.security.HasUser;
import com.github.kshashov.timetracker.web.security.ProjectPermission;
import com.github.kshashov.timetracker.web.ui.util.DataHandler;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import rx.Observable;
import rx.subjects.BehaviorSubject;

import java.util.function.Function;

@UIScope
@SpringComponent
public class ProjectInfoViewModel implements HasUser, DataHandler {
    private final RolePermissionsHelper rolePermissionsHelper;

    private final BehaviorSubject<Project> projectObservable = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> hasAccessObservable = BehaviorSubject.create();

    @Autowired
    public ProjectInfoViewModel(RolePermissionsHelper rolePermissionsHelper) {
        this.rolePermissionsHelper = rolePermissionsHelper;
    }

    public void setProject(Project project, Role role) {
        checkAccess(role);
        projectObservable.onNext(project);
    }

    public Observable<Boolean> hasAccess() {
        return hasAccessObservable;
    }

    public Observable<Project> project() {
        return projectObservable;
    }

    private boolean checkAccess(Role role) {
        boolean hasAccess = rolePermissionsHelper.hasPermission(role, ProjectPermission.EDIT_PROJECT_INFO);
        hasAccessObservable.onNext(hasAccess);
        return hasAccess;
    }

    @Getter
    @AllArgsConstructor
    public static class ProjectDialog {
        private final Project project;
        private final Function<Project, ValidationResult> validator;
    }
}
