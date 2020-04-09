package com.github.kshashov.timetracker.web.ui.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import com.github.kshashov.timetracker.web.security.HasUser;
import com.github.kshashov.timetracker.web.ui.util.CrudEntity;
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

    private final BehaviorSubject<CrudEntity<Project>> projectObservable = BehaviorSubject.create();

    @Autowired
    public ProjectInfoViewModel(RolePermissionsHelper rolePermissionsHelper) {
        this.rolePermissionsHelper = rolePermissionsHelper;
    }

    public void setProject(Project project, Role role) {
        CrudEntity.CrudAccess access = checkAccess(role);

        if (access.equals(CrudEntity.CrudAccess.DENIED)) {
            projectObservable.onNext(new CrudEntity<>(null, access));
            return;
        }

        projectObservable.onNext(new CrudEntity<>(project, access));
    }

    public Observable<CrudEntity<Project>> project() {
        return projectObservable;
    }

    private CrudEntity.CrudAccess checkAccess(Role role) {
        if (rolePermissionsHelper.hasPermission(role, ProjectPermissionType.VIEW_PROJECT_INFO)) {
            return CrudEntity.CrudAccess.READ_ONLY;
        }

        return CrudEntity.CrudAccess.DENIED;
    }
}
