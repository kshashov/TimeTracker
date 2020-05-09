package com.github.kshashov.timetracker.data.service.admin.roles;

import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRoleInfo {
    private Long roleId;

    public ProjectRoleInfo(ProjectRole bean) {
        setRoleId(bean.getRole().getId());
    }
}
