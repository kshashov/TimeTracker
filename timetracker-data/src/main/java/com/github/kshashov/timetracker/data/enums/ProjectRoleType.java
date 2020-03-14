package com.github.kshashov.timetracker.data.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProjectRoleType {
    ADMIN("project_admin");

    private final String code;

    public String getCode() {
        return code;
    }
}
