package com.github.kshashov.timetracker.data.service;

import com.github.kshashov.timetracker.core.errors.IncorrectArgumentException;
import com.github.kshashov.timetracker.core.errors.NoPermissionException;
import com.github.kshashov.timetracker.data.entity.Entry;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.repo.EntriesRepository;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@Service
public class EntriesServiceImpl implements EntriesService {
    private final RolePermissionsHelper rolePermissionsHelper;
    private final EntriesRepository entriesRepository;

    @Autowired
    public EntriesServiceImpl(RolePermissionsHelper rolePermissionsHelper, EntriesRepository entriesRepository) {
        this.rolePermissionsHelper = rolePermissionsHelper;
        this.entriesRepository = entriesRepository;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public Entry createEntry(@NotNull User user, @NotNull Entry entry) {
        preValidate(entry);

        Project project = entry.getAction().getProject();

        boolean sameUser = user.getId().equals(entry.getUser().getId());
        if ((project == null) || (sameUser && !rolePermissionsHelper.hasProjectPermission(user, project, ProjectPermissionType.EDIT_MY_LOGS))) {
            throw new NoPermissionException("You have no permissions to create work items for this project");
        } else if (!sameUser) {
            boolean hasProjectPermission = rolePermissionsHelper.hasProjectPermission(user, project, ProjectPermissionType.VIEW_PROJECT_LOGS);
            if (!hasProjectPermission) {
                throw new NoPermissionException("You have no permissions to create other people's work items for this project");
            }
        }

        return doCreateEntry(entry);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public Entry updateEntry(@NotNull User user, @NotNull Entry entry) {
        preValidate(entry);

        Project project = entry.getAction().getProject();

        boolean sameUser = user.getId().equals(entry.getUser().getId());
        if ((project == null) || (sameUser && !rolePermissionsHelper.hasProjectPermission(user, project, ProjectPermissionType.EDIT_MY_LOGS))) {
            throw new NoPermissionException("You have no permissions to update work items for this project");
        } else if (!sameUser) {
            boolean hasProjectPermission = rolePermissionsHelper.hasProjectPermission(user, project, ProjectPermissionType.VIEW_PROJECT_LOGS);
            if (!hasProjectPermission) {
                throw new NoPermissionException("You have no permissions to update other people's work items for this project");
            }
        }

        return doUpdateEntry(entry);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public void deleteEntry(@NotNull User user, @NotNull Long entryId) {
        Entry entry = entriesRepository.findWithUserAndProjectById(entryId);
        Project project = entry.getAction().getProject();

        Objects.requireNonNull(entry);

        boolean sameUser = user.getId().equals(entry.getUser().getId());
        if (sameUser && !rolePermissionsHelper.hasProjectPermission(user, project, ProjectPermissionType.EDIT_MY_LOGS)) {
            throw new NoPermissionException("You have no permissions to delete work items for this project");
        } else if (!sameUser) {
            boolean hasProjectPermission = rolePermissionsHelper.hasProjectPermission(user, project, ProjectPermissionType.VIEW_PROJECT_LOGS);
            if (!hasProjectPermission) {
                throw new NoPermissionException("You have no permissions to delete other people's work items for this project");
            }
        }

        doDeleteEntry(entry);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public Entry createEntry(@NotNull Entry entry) {
        preValidate(entry);
        return doCreateEntry(entry);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public Entry updateEntry(@NotNull Entry entry) {
        preValidate(entry);
        return doUpdateEntry(entry);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public void deleteEntry(@NotNull Long entryId) {
        Entry entry = entriesRepository.findWithUserAndProjectById(entryId);
        doDeleteEntry(entry);
    }

    private Entry doCreateEntry(@NotNull Entry entry) {
        // Validate
        if (entry.getId() != null) {
            throw new IllegalArgumentException();
        }

        entry = entriesRepository.save(entry);
        return entry;
    }

    private Entry doUpdateEntry(@NotNull Entry entry) {
        // Validate
        if (entry.getId() == null) {
            throw new IllegalArgumentException();
        }

        entry = entriesRepository.save(entry);
        return entry;
    }

    private void doDeleteEntry(@NotNull Entry entry) {
        // Validate
        if (entry.getId() == null) {
            throw new IllegalArgumentException();
        }

        if (entry.getIsClosed()) {
            throw new IncorrectArgumentException("Closed entry can't be deleted");
        }

        entriesRepository.deleteById(entry.getId());
    }

    private void preValidate(@NotNull Entry entry) {
        if (entry.getHours() == null) {
            throw new IncorrectArgumentException("Work item hours is empty");
        } else if (entry.getHours() <= 0) {
            throw new IncorrectArgumentException("Work item hours can't be less or equal to 0");
        }

        if (entry.getIsClosed()) {
            throw new IncorrectArgumentException("Closed entry can't be updated");
        }

        if (Strings.isBlank(entry.getTitle())) {
            throw new IncorrectArgumentException("Work item description is empty");
        }

        if (entry.getObs() == null) {
            throw new IncorrectArgumentException("Work item date is empty");
        }

        if (entry.getAction() == null) {
            throw new IncorrectArgumentException("Work item action is empty");
        }

        if (entry.getUser() == null) {
            throw new IncorrectArgumentException("Work item user is empty");
        }
    }
}
