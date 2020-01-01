package com.github.kshashov.timetracker.data.repo.user;

import com.github.kshashov.timetracker.data.entity.user.ProjectPermission;
import com.github.kshashov.timetracker.data.entity.user.ProjectPermissionIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectPermissionsRepository extends JpaRepository<ProjectPermission, ProjectPermissionIdentity> {
}
