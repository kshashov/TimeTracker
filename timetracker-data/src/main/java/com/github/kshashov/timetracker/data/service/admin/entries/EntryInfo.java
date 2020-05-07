package com.github.kshashov.timetracker.data.service.admin.entries;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EntryInfo {
    private LocalDate obs;
    private Double hours;
    private String title;
    private Long actionId;
}
