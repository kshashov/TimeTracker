package com.github.kshashov.timetracker.data.service.admin.days;

import com.github.kshashov.timetracker.data.entity.user.User;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public interface AuthorizedClosedDaysService {
    void openDays(@NotNull User user, @NotNull Long projectId, @NotNull LocalDate from, @NotNull LocalDate to);

    void closeDays(@NotNull User user, @NotNull Long projectId, @NotNull LocalDate from, @NotNull LocalDate to);
}
