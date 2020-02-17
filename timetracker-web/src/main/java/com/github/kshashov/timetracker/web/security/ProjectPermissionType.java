package com.github.kshashov.timetracker.web.security;


import com.github.kshashov.timetracker.data.utils.HasPermissionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProjectPermissionType implements HasPermissionCode {
    EDIT_MY_LOGS("edit_my_logs"),
    EDIT_LOGS("edit_project_logs"),
    EDIT_PROJECT_INFO("edit_project_info"),
    EDIT_PROJECT_ACTIONS("edit_project_actions"),
    EDIT_PROJECT_USERS("edit_project_users");

    private final String code;

    @Override
    public String getPemissionCode() {
        return code;
    }
}
