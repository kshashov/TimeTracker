package com.github.kshashov.timetracker.data.utils;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.Permission;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.repo.user.ProjectRolesRepository;
import com.github.kshashov.timetracker.data.repo.user.RolesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = "rolePermission")
public class RolePermissionsHelper {
    private final RolesRepository rolesRepository;
    private final ProjectRolesRepository projectRolesRepository;
    private Map<Long, Set<String>> roles;

    @Autowired
    public RolePermissionsHelper(RolesRepository rolesRepository, ProjectRolesRepository projectRolesRepository) {
        this.projectRolesRepository = projectRolesRepository;
        this.rolesRepository = rolesRepository;
        reload();
    }

    private void reload() {
        this.roles = rolesRepository.findAll().stream()
                .collect(Collectors.toMap(
                        Role::getId,
                        r -> r.getPermissions().stream()
                                .map(Permission::getCode)
                                .collect(Collectors.toSet())
                ));
    }

    @Cacheable(key = "#role.id + #permission.pemissionCode")
    public boolean hasPermission(Role role, HasPermissionCode permission) {
        return roles.containsKey(role.getId()) && roles.get(role.getId()).contains(permission.getPemissionCode());
    }

    public boolean hasProjectPermission(User user, Project project, ProjectPermissionType projectPermission) {
        return hasProjectPermission(user, project.getId(), projectPermission);
    }

    private boolean hasProjectPermission(User user, Long projectId, ProjectPermissionType projectPermission) {
        ProjectRole projectRole = projectRolesRepository.findOneByUserIdAndProjectId(user.getId(), projectId);
        if (projectRole == null) {
            return false;
        }

        return hasPermission(projectRole.getRole(), projectPermission);
    }
}
