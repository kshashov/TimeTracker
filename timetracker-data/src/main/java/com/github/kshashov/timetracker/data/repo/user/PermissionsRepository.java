package com.github.kshashov.timetracker.data.repo.user;

import com.github.kshashov.timetracker.data.entity.user.Permission;
import com.github.kshashov.timetracker.data.repo.BaseRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionsRepository extends JpaRepository<Permission, Long>, BaseRepo {
}
