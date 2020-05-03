package com.github.kshashov.timetracker.web.ui.views.admin.projects;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import com.github.kshashov.timetracker.web.security.HasUser;
import com.github.kshashov.timetracker.web.ui.util.CrudEntity;
import com.github.kshashov.timetracker.web.ui.util.DataHandler;
import com.google.common.eventbus.EventBus;
import com.vaadin.flow.spring.annotation.SpringComponent;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import rx.Observable;
import rx.subjects.BehaviorSubject;

@Slf4j
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SpringComponent
public class ProjectInfoViewModel implements HasUser, DataHandler {
    private final RolePermissionsHelper rolePermissionsHelper;
    private final EventBus eventBus;

    private final BehaviorSubject<CrudEntity<Project>> projectObservable = BehaviorSubject.create();

    @Autowired
    public ProjectInfoViewModel(RolePermissionsHelper rolePermissionsHelper, EventBus eventBus) {
        this.rolePermissionsHelper = rolePermissionsHelper;
        this.eventBus = eventBus;
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

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public EventBus eventBus() {
        return eventBus;
    }
}
