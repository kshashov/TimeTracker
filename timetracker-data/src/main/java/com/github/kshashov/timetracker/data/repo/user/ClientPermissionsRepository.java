package com.github.kshashov.timetracker.data.repo.user;

import com.github.kshashov.timetracker.data.entity.user.ClientPermission;
import com.github.kshashov.timetracker.data.entity.user.ClientPermissionIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientPermissionsRepository extends JpaRepository<ClientPermission, ClientPermissionIdentity> {
}
