package com.github.kshashov.timetracker.data.service;

import com.github.kshashov.timetracker.data.entity.Entry;
import com.github.kshashov.timetracker.data.entity.user.User;

import javax.validation.constraints.NotNull;

public interface EntriesService {
    Entry createEntry(@NotNull Entry entry);

    Entry createEntry(@NotNull User user, @NotNull Entry entry);

    Entry updateEntry(@NotNull Entry entry);

    Entry updateEntry(@NotNull User user, @NotNull Entry entry);

    void deleteEntry(@NotNull Long entryId);

    void deleteEntry(@NotNull User user, @NotNull Long entryId);
}
