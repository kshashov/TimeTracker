package com.github.kshashov.timetracker.data.utils;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.Permission;
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
    private final ProjectRolesRepository projectRolesRepository;
    private final Map<Long, Set<String>> roles;

    @Autowired
    public RolePermissionsHelper(RolesRepository rolesRepository, ProjectRolesRepository projectRolesRepository) {
        this.projectRolesRepository = projectRolesRepository;
        this.roles = rolesRepository.findAllWithPermissions().stream()
                .collect(Collectors.toMap(
                        Role::getId,
                        r -> r.getPermissions().stream()
                                .map(Permission::getCode)
                                .collect(Collectors.toSet())
                ));
    }

    @Cacheable(key = "#role.id + #permission.pemissionCode")
    public boolean hasPermission(Role role, HasPermissionCode permission) {
        return roles.get(role.getId()).contains(permission.getPemissionCode());
    }

    public Boolean hasProjectPermission(User user, Project project, ProjectPermissionType projectPermission) {
        return projectRolesRepository.findFirstByUserAndProject(user, project)
                .map(pr -> hasPermission(pr.getRole(), projectPermission))
                .orElse(false);
    }
}
