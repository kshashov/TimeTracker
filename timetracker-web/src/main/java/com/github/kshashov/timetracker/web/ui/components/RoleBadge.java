package com.github.kshashov.timetracker.web.ui.components;

import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.data.enums.ProjectRoleType;
import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.github.kshashov.timetracker.web.ui.util.css.lumo.BadgeColor;

public class RoleBadge extends Badge {

    public RoleBadge(Role role) {
        super(role.getTitle(), getBadgeColor(role));
        UIUtils.addTooltip(this, role.getDescription());
    }

    private static BadgeColor getBadgeColor(Role role) {
        if (ProjectRoleType.isAdmin(role)) {
            return BadgeColor.SUCCESS;
        } else if (ProjectRoleType.isInactive(role)) {
            return BadgeColor.ERROR;
        }

        return BadgeColor.NORMAL;
    }
}
