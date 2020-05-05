package com.github.kshashov.timetracker.data.service.admin.projects;

import com.github.kshashov.timetracker.data.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectInfo implements BaseEntity {
    private String title;
}
