package com.github.kshashov.timetracker.data;

import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.enums.ProjectRoleType;
import com.github.kshashov.timetracker.data.repo.user.PermissionsRepository;
import com.github.kshashov.timetracker.data.repo.user.RolesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class ContextRefreshedListener implements ApplicationListener<ContextRefreshedEvent> {

    private final PermissionsRepository permissionsRepository;
    private final RolesRepository rolesRepository;

    @Autowired
    public ContextRefreshedListener(PermissionsRepository permissionsRepository, RolesRepository rolesRepository) {
        this.permissionsRepository = permissionsRepository;
        this.rolesRepository = rolesRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        // Check permissions
        for (ProjectPermissionType permissionType : ProjectPermissionType.values()) {
            if (permissionsRepository.findOneByCode(permissionType.getCode()) == null) {
                throw new IllegalStateException(String.format("Permission '%s' is missing in the database", permissionType.getCode()));
            }
        }

        // Check roles
        for (ProjectRoleType roleType : ProjectRoleType.values()) {
            if (rolesRepository.findOneByCode(roleType.getCode()) == null) {
                throw new IllegalStateException(String.format("Role '%s' is missing in the database", roleType.getCode()));
            }
        }
    }
}
