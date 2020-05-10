package com.github.kshashov.timetracker.data.service.admin.entries;

import com.github.kshashov.timetracker.data.entity.Entry;
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

    public EntryInfo(Entry entry) {
        setObs(entry.getObs());
        setTitle(entry.getTitle());
        setHours(entry.getHours());
        if (entry.getAction() != null) {
            setActionId(entry.getAction().getId());
        }
    }
}
