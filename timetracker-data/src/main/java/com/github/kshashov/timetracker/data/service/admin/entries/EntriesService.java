package com.github.kshashov.timetracker.data.service.admin.entries;

import com.github.kshashov.timetracker.data.entity.Entry;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public interface EntriesService {
    Entry createEntry(@NotNull Long userId, @NotNull EntryInfo entryInfo);

    Entry updateEntry(@NotNull Long entryId, @NotNull EntryInfo entryInfo);

    void openEntries(@NotNull Long projectId, @NotNull LocalDate from, @NotNull LocalDate to);

    void closeEntries(@NotNull Long projectId, @NotNull LocalDate from, @NotNull LocalDate to);

    void deleteEntry(@NotNull Long entryId);
}
