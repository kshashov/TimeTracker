package com.github.kshashov.timetracker.data.service.admin.entries;

import com.github.kshashov.timetracker.data.entity.Entry;
import com.github.kshashov.timetracker.data.entity.user.User;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public interface AuthorizedEntriesService {
    Entry createEntry(@NotNull User user, @NotNull Long userId, @NotNull EntryInfo entryInfo);

    Entry updateEntry(@NotNull User user, @NotNull Long entryId, @NotNull EntryInfo entryInfo);

    void openEntries(@NotNull User user, @NotNull Long projectId, @NotNull LocalDate from, @NotNull LocalDate to);

    void closeEntries(@NotNull User user, @NotNull Long projectId, @NotNull LocalDate from, @NotNull LocalDate to);

    void deleteEntry(@NotNull User user, @NotNull Long entryId);
}
