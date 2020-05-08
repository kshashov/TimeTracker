package com.github.kshashov.timetracker.data.service.admin.days;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public interface ClosedDaysService {
    void openDays(@NotNull Long projectId, @NotNull LocalDate from, @NotNull LocalDate to);

    void closeDays(@NotNull Long projectId, @NotNull LocalDate from, @NotNull LocalDate to);
}
