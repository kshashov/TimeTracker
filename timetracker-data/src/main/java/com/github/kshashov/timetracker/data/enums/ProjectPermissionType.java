package com.github.kshashov.timetracker.data.enums;


import com.github.kshashov.timetracker.data.utils.HasPermissionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProjectPermissionType implements HasPermissionCode {
    VIEW_PROJECT_INFO("view_project_info"),
    EDIT_MY_LOGS("edit_my_logs"),
    VIEW_PROJECT_LOGS("view_project_logs"),
    EDIT_PROJECT_LOGS("edit_project_logs"),
    EDIT_PROJECT_INFO("edit_project_info"),
    EDIT_PROJECT_ACTIONS("edit_project_actions"),
    EDIT_PROJECT_USERS("edit_project_users");

    private final String code;

    @Override
    public String getPemissionCode() {
        return code;
    }
}
