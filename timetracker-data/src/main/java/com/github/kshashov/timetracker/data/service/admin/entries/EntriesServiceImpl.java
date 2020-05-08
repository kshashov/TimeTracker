package com.github.kshashov.timetracker.data.service.admin.entries;

import com.github.kshashov.timetracker.core.errors.IncorrectArgumentException;
import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Entry;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.Permission;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.repo.ActionsRepository;
import com.github.kshashov.timetracker.data.repo.ClosedDaysRepository;
import com.github.kshashov.timetracker.data.repo.EntriesRepository;
import com.github.kshashov.timetracker.data.repo.ProjectsRepository;
import com.github.kshashov.timetracker.data.repo.user.PermissionsRepository;
import com.github.kshashov.timetracker.data.repo.user.UsersRepository;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Service
public class EntriesServiceImpl implements EntriesService {
    private final RolePermissionsHelper rolePermissionsHelper;
    private final EntriesRepository entriesRepository;
    private final ActionsRepository actionsRepository;
    private final UsersRepository usersRepository;
    private final ProjectsRepository projectsRepository;
    private final PermissionsRepository permissionsRepository;
    private final Permission selfPermission;
    private final ClosedDaysRepository closedDaysRepository;

    @Autowired
    public EntriesServiceImpl(
            RolePermissionsHelper rolePermissionsHelper,
            EntriesRepository entriesRepository,
            ActionsRepository actionsRepository,
            UsersRepository usersRepository,
            ProjectsRepository projectsRepository,
            PermissionsRepository permissionsRepository,
            ClosedDaysRepository closedDaysRepository) {
        this.rolePermissionsHelper = rolePermissionsHelper;
        this.entriesRepository = entriesRepository;
        this.actionsRepository = actionsRepository;
        this.usersRepository = usersRepository;
        this.projectsRepository = projectsRepository;
        this.permissionsRepository = permissionsRepository;
        this.closedDaysRepository = closedDaysRepository;
        this.selfPermission = this.permissionsRepository.findOneByCode(ProjectPermissionType.EDIT_MY_LOGS.getCode());


    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public Entry createEntry(@NotNull Long userId, @NotNull EntryInfo entryInfo) {
        return doCreateEntry(userId, entryInfo);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public Entry updateEntry(@NotNull Long entryId, @NotNull EntryInfo entryInfo) {
        return doUpdateEntry(entryId, entryInfo);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public void deleteEntry(@NotNull Long entryId) {
        doDeleteEntry(entryId);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public void openEntries(@NotNull Long projectId, @NotNull LocalDate from, @NotNull LocalDate to) {
        doOpenEntries(projectId, from, to);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public void closeEntries(@NotNull Long projectId, @NotNull LocalDate from, @NotNull LocalDate to) {
        doCloseEntries(projectId, from, to);
    }

    private Entry doCreateEntry(@NotNull Long userId, @NotNull EntryInfo entryInfo) {
        preValidate(entryInfo);

        Action action = actionsRepository.getOne(entryInfo.getActionId());
        User user = usersRepository.getOne(userId);

        Long closed = closedDaysRepository.countByProjectAndIdentityObsBetween(action.getProject(), entryInfo.getObs(), entryInfo.getObs());
        if (closed > 0) {
            throw new IncorrectArgumentException("Closed day can't be updated");
        }

        if (!action.getIsActive()) {
            throw new IncorrectArgumentException("Inactive action can't be updated");
        }

        if (!action.getProject().getIsActive()) {
            throw new IncorrectArgumentException("Inactive project can't be updated");
        }

        Entry entry = new Entry();
        entry.setIsClosed(false);
        entry.setObs(entryInfo.getObs());
        entry.setHours(entryInfo.getHours());
        entry.setTitle(entryInfo.getTitle());
        entry.setUser(user);
        entry.setAction(action);
        entry = entriesRepository.save(entry);
        return entry;
    }

    private Entry doUpdateEntry(@NotNull Long entryId, @NotNull EntryInfo entryInfo) {
        preValidate(entryInfo);

        Entry entry = entriesRepository.getOne(entryId);

        if (entry.getIsClosed()) {
            throw new IncorrectArgumentException("Closed entry can't be updated");
        }

        if (!entry.getAction().getIsActive()) {
            throw new IncorrectArgumentException("Inactive action can't be updated");
        }

        if (!entry.getAction().getProject().getIsActive()) {
            throw new IncorrectArgumentException("Inactive project can't be updated");
        }

        Action action = actionsRepository.getOne(entryInfo.getActionId());
        entry.setObs(entryInfo.getObs());
        entry.setHours(entryInfo.getHours());
        entry.setTitle(entryInfo.getTitle());
        entry.setAction(action);
        entry = entriesRepository.save(entry);
        return entry;
    }

    private void doDeleteEntry(@NotNull Long entryId) {
        Entry entry = entriesRepository.getOne(entryId);

        if (entry.getIsClosed()) {
            throw new IncorrectArgumentException("Closed entry can't be deleted");
        }

        if (!entry.getAction().getIsActive()) {
            throw new IncorrectArgumentException("Inactive action can't be updated");
        }

        if (!entry.getAction().getProject().getIsActive()) {
            throw new IncorrectArgumentException("Inactive project can't be updated");
        }

        entriesRepository.deleteById(entry.getId());
    }

    private void doOpenEntries(@NotNull Long projectId, @NotNull LocalDate from, @NotNull LocalDate to) {
        Project project = projectsRepository.getOne(projectId);
        if (!project.getIsActive()) {
            throw new IncorrectArgumentException("Inactive project can't be updated");
        }
        // Open entries with:
        // - specified project
        // - user with edit_my_logs permission
        // - open state
        // - active project
        // - active action
        entriesRepository.openByProjectAndUserPermission(project, selfPermission, from, to);
    }

    private void doCloseEntries(@NotNull Long projectId, @NotNull LocalDate from, @NotNull LocalDate to) {
        Project project = projectsRepository.getOne(projectId);
        if (!project.getIsActive()) {
            throw new IncorrectArgumentException("Inactive project can't be updated");
        }
        // Close entries with:
        // - specified project
        // - user with edit_my_logs permission
        // - open state
        // - active project
        // - active action
        entriesRepository.closeByProjectAndUserPermission(project, selfPermission, from, to);
    }

    private void preValidate(@NotNull EntryInfo entryInfo) {
        if (entryInfo.getHours() == null) {
            throw new IncorrectArgumentException("Work item hours is empty");
        } else if (entryInfo.getHours() <= 0) {
            throw new IncorrectArgumentException("Work item hours can't be less or equal to 0");
        }

        if (Strings.isBlank(entryInfo.getTitle())) {
            throw new IncorrectArgumentException("Work item description is empty");
        }

        if (entryInfo.getObs() == null) {
            throw new IncorrectArgumentException("Work item date is empty");
        }

        if (entryInfo.getActionId() == null) {
            throw new IncorrectArgumentException("Work item action is empty");
        }
    }
}
