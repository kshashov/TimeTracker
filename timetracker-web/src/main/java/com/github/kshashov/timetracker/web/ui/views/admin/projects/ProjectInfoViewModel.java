package com.github.kshashov.timetracker.web.ui.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import com.github.kshashov.timetracker.web.security.HasUser;
import com.github.kshashov.timetracker.web.security.ProjectPermission;
import com.github.kshashov.timetracker.web.ui.util.DataHandler;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import rx.Observable;
import rx.subjects.BehaviorSubject;

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

    public Observable<Project> project() {
        return projectObservable;
    }

    public Observable<Boolean> hasAccess() {
        return hasAccessObservable;
    }

    private boolean checkAccess(Role role) {
        boolean hasAccess = rolePermissionsHelper.hasPermission(role, ProjectPermission.EDIT_MY_LOGS);
        hasAccessObservable.onNext(hasAccess);
        return hasAccess;
    }
}
