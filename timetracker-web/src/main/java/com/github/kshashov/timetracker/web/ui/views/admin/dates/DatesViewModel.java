package com.github.kshashov.timetracker.web.ui.views.admin.dates;

import com.github.kshashov.timetracker.data.entity.ClosedDay;
import com.github.kshashov.timetracker.data.entity.ClosedDayIdentity;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.Permission;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.repo.ClosedDaysRepository;
import com.github.kshashov.timetracker.data.repo.EntriesRepository;
import com.github.kshashov.timetracker.data.repo.user.PermissionsRepository;
import com.github.kshashov.timetracker.data.repo.user.ProjectRolesRepository;
import com.github.kshashov.timetracker.web.security.HasUser;
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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SpringComponent
public class DatesViewModel implements HasUser, DataHandler {
    private final EventBus eventBus;
    private final EntriesRepository entriesRepository;
    private final ClosedDaysRepository closedDaysRepository;
    private final PermissionsRepository permissionsRepository;
    private final ProjectRolesRepository projectRolesRepository;

    private final Permission selfPermission;
    private final Permission permission;
    private final User user;
    private Project project;
    private LocalDate from;
    private LocalDate to;

    private final BehaviorSubject<List<Project>> projectsObservable = BehaviorSubject.create();
    private final BehaviorSubject<Map<VisibilityItem, Boolean>> visibilityObservable = BehaviorSubject.create();

    @Autowired
    public DatesViewModel(
            EventBus eventBus,
            EntriesRepository entriesRepository,
            ClosedDaysRepository closedDaysRepository,
            PermissionsRepository permissionsRepository,
            ProjectRolesRepository projectRolesRepository) {
        this.eventBus = eventBus;
        this.entriesRepository = entriesRepository;
        this.closedDaysRepository = closedDaysRepository;
        this.permissionsRepository = permissionsRepository;
        this.projectRolesRepository = projectRolesRepository;

        this.user = getUser();

        selfPermission = permissionsRepository.findOneByCode(ProjectPermissionType.EDIT_MY_LOGS.getCode());
        permission = permissionsRepository.findOneByCode(ProjectPermissionType.VIEW_PROJECT_LOGS.getCode());

        updateVisibility();
        reloadProjects();
    }

    public void setDates(LocalDate from, LocalDate to) {
        this.from = from;
        this.to = to;

        updateVisibility();
    }

    public void setProject(Project project) {
        this.project = project;

        updateVisibility();
    }

    public void openDays() {
        Long count = closedDaysRepository.deleteByProjectAndIdentityObsBetween(project, from, to);

        updateVisibility();
    }

    public void closeDays() {
        closedDaysRepository.deleteByProjectAndIdentityObsBetween(project, from, to);

        LocalDate from = this.from;
        while (!from.isAfter(to)) {
            ClosedDayIdentity identity = new ClosedDayIdentity();
            identity.setProjectId(project.getId());
            identity.setObs(from);

            ClosedDay day = new ClosedDay();
            day.setIdentity(identity);

            closedDaysRepository.save(day);
            from = from.plusDays(1);
        }

        updateVisibility();
    }

    public void openEntries() {
        entriesRepository.openByProjectAndUserPermission(project, selfPermission, from, to);

        updateVisibility();
    }

    public void closeEntries() {
        entriesRepository.closeByProjectAndUserPermission(project, selfPermission, from, to);

        updateVisibility();
    }

    private void reloadProjects() {
        List<Project> projects = projectRolesRepository
                .findWithActionsByUserAndRolePermissionsContains(user, permission)
                .stream()
                .map(ProjectRole::getProject)
                .collect(Collectors.toList());
        projectsObservable.onNext(projects);
    }

    private void updateVisibility() {
        if ((project == null) || (from == null) || (to == null)) {
            disable();
            return;
        } else if (from.isAfter(to)) {
            disable();
            return;
        }

        long days = ChronoUnit.DAYS.between(from, to) + 1;
        long closedDays = closedDaysRepository.countByProjectAndIdentityObsBetween(project, from, to);

        long entries = entriesRepository.countByProjectAndUserPermission(project, selfPermission, from, to);
        long closedEntries = entriesRepository.countClosedByProjectAndUserPermission(project, selfPermission, from, to);

        Map<VisibilityItem, Boolean> map = new HashMap<>();
        map.put(VisibilityItem.OPEN_DAYS, closedDays > 0);
        map.put(VisibilityItem.CLOSE_DAYS, closedDays < days);
        map.put(VisibilityItem.OPEN_ENTRIES, closedEntries > 0);
        map.put(VisibilityItem.CLOSE_ENTRIES, closedEntries < entries);
        visibilityObservable.onNext(map);
    }

    private void disable() {
        Map<VisibilityItem, Boolean> map = new HashMap<>();
        map.put(VisibilityItem.OPEN_DAYS, false);
        map.put(VisibilityItem.CLOSE_DAYS, false);
        map.put(VisibilityItem.OPEN_ENTRIES, false);
        map.put(VisibilityItem.CLOSE_ENTRIES, false);
        visibilityObservable.onNext(map);
    }

    public Observable<List<Project>> projects() {
        return projectsObservable;
    }

    public Observable<Map<VisibilityItem, Boolean>> visibility() {
        return visibilityObservable;
    }

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public EventBus eventBus() {
        return eventBus;
    }

    public enum VisibilityItem {
        OPEN_DAYS, CLOSE_DAYS, OPEN_ENTRIES, CLOSE_ENTRIES
    }
}
