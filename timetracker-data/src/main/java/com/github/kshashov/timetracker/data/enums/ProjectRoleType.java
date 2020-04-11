package com.github.kshashov.timetracker.data.enums;


import com.github.kshashov.timetracker.data.entity.user.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProjectRoleType {
    ADMIN("project_admin"),
    INACTIVE("project_inactive");

    private final String code;

    public String getCode() {
        return code;
    }

    public static boolean isInactive(Role role) {
        return role.getCode().equals(INACTIVE.getCode());
    }

    public static boolean isAdmin(Role role) {
        return role.getCode().equals(ADMIN.getCode());
    }
}
