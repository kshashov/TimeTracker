package com.github.kshashov.timetracker.web.ui.views.entries;

import com.github.kshashov.timetracker.data.entity.Entry;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.Permission;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.repo.EntriesRepository;
import com.github.kshashov.timetracker.data.repo.user.PermissionsRepository;
import com.github.kshashov.timetracker.data.repo.user.ProjectRolesRepository;
import com.github.kshashov.timetracker.data.repo.user.RolesRepository;
import com.github.kshashov.timetracker.data.service.EntriesService;
import com.github.kshashov.timetracker.web.security.HasUser;
import com.github.kshashov.timetracker.web.ui.util.DataHandler;
import com.vaadin.flow.data.binder.ValidationResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
public class DailyEntriesViewModel implements HasUser, DataHandler {
    private final ProjectRolesRepository projectRolesRepository;
    private final EntriesRepository entriesRepository;
    private final EntriesService entriesService;

    private final User user;
    private LocalDate date;

    private final PublishSubject<CreateEntryDialog> createEntryDialogObservable = PublishSubject.create();
    private final PublishSubject<UpdateEntryDialog> updateEntryDialogObservable = PublishSubject.create();
    private final BehaviorSubject<List<Entry>> entriesObservable = BehaviorSubject.create();
    private final BehaviorSubject<List<Project>> projectsObservable = BehaviorSubject.create();
    private final RolesRepository rolesRepository;
    private final PermissionsRepository permissionsRepository;

    public DailyEntriesViewModel(
            EntriesRepository entriesRepository,
            ProjectRolesRepository projectRolesRepository,
            EntriesService entriesService,
            RolesRepository rolesRepository,
            PermissionsRepository permissionsRepository) {
        this.entriesRepository = entriesRepository;
        this.projectRolesRepository = projectRolesRepository;
        this.entriesService = entriesService;
        this.rolesRepository = rolesRepository;
        this.permissionsRepository = permissionsRepository;
        this.user = getUser();
    }

    public void setDate(@NotNull LocalDate date) {
        this.date = date;

        reloadProjects();
        reloadEntries();
    }

    public void createEntry() {
        Entry entry = new Entry();
        entry.setUser(user);
        entry.setObs(date);
        entry.setIsClosed(false);

        createEntryDialogObservable.onNext(new CreateEntryDialog(
                entry,
                bean -> handleDataManipulation(
                        () -> entriesService.createEntry(user, bean),
                        result -> reloadEntries())
        ));
    }

    public void updateEntry(Entry entry) {
        updateEntryDialogObservable.onNext(new UpdateEntryDialog(
                entry,
                bean -> handleDataManipulation(
                        () -> entriesService.updateEntry(user, bean),
                        result -> reloadEntries())
        ));
    }

    public void deleteEntry(Entry action) {
        handleDataManipulation(
                () -> entriesService.deleteEntry(user, action.getId()),
                () -> reloadEntries());
    }

    public void reloadEntries() {
        List<Entry> entries = entriesRepository.findFullByUserAndObs(user, date);

        entriesObservable.onNext(entries);
    }

    private void reloadProjects() {
        Permission permission = permissionsRepository.findOneByCode(ProjectPermissionType.EDIT_MY_LOGS.getCode());

        List<Project> projects = projectRolesRepository
                .findWithActionsByUserAndRolePermissionsContains(user, permission)
                .stream()
                .map(ProjectRole::getProject)
                .collect(Collectors.toList());
        projectsObservable.onNext(projects);
    }

    public Observable<List<Entry>> entries() {
        return entriesObservable;
    }

    public Observable<List<Project>> projects() {
        return projectsObservable;
    }

    public Observable<CreateEntryDialog> createEntryDialog() {
        return createEntryDialogObservable;
    }

    public Observable<UpdateEntryDialog> updateEntryDialog() {
        return updateEntryDialogObservable;
    }

    @Getter
    @AllArgsConstructor
    public static class CreateEntryDialog {
        private final Entry entry;
        private final Function<Entry, ValidationResult> validator;
    }

    @Getter
    @AllArgsConstructor
    public static class UpdateEntryDialog {
        private final Entry entry;
        private final Function<Entry, ValidationResult> validator;
    }
}
