package com.github.kshashov.timetracker.data.service.admin.entries;

import com.github.kshashov.timetracker.data.entity.Entry;

import javax.validation.constraints.NotNull;

public interface EntriesService {
    Entry createEntry(@NotNull Long userId, @NotNull EntryInfo entryInfo);

    Entry updateEntry(@NotNull Long entryId, @NotNull EntryInfo entryInfo);

    void deleteEntry(@NotNull Long entryId);
}
