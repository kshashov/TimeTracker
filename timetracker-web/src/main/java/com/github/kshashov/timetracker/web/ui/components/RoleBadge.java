package com.github.kshashov.timetracker.web.ui.components;

import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.data.enums.ProjectRoleType;
import com.github.kshashov.timetracker.web.ui.util.css.lumo.BadgeColor;

public class RoleBadge extends Badge {

    public RoleBadge(Role role) {
        super(role.getCode(), getBadgeColor(role));
    }

    private static BadgeColor getBadgeColor(Role role) {
        if (role.getCode().equals(ProjectRoleType.ADMIN.getCode())) {
            return BadgeColor.SUCCESS;
        }

        return BadgeColor.NORMAL;
    }
}
