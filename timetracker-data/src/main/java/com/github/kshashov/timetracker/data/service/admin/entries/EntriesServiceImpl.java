package com.github.kshashov.timetracker.data.service.admin.entries;

import com.github.kshashov.timetracker.core.errors.IncorrectArgumentException;
import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Entry;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.repo.ActionsRepository;
import com.github.kshashov.timetracker.data.repo.EntriesRepository;
import com.github.kshashov.timetracker.data.repo.user.UsersRepository;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;

@Service
public class EntriesServiceImpl implements EntriesService {
    private final RolePermissionsHelper rolePermissionsHelper;
    private final EntriesRepository entriesRepository;
    private final ActionsRepository actionsRepository;
    private final UsersRepository usersRepository;

    @Autowired
    public EntriesServiceImpl(
            RolePermissionsHelper rolePermissionsHelper,
            EntriesRepository entriesRepository,
            ActionsRepository actionsRepository,
            UsersRepository usersRepository) {
        this.rolePermissionsHelper = rolePermissionsHelper;
        this.entriesRepository = entriesRepository;
        this.actionsRepository = actionsRepository;
        this.usersRepository = usersRepository;
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

    private Entry doCreateEntry(@NotNull Long userId, @NotNull EntryInfo entryInfo) {
        preValidate(entryInfo);

        Action action = actionsRepository.getOne(entryInfo.getActionId());
        User user = usersRepository.getOne(userId);

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

        entriesRepository.deleteById(entry.getId());
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
