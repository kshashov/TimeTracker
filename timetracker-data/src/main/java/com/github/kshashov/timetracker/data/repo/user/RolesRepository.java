package com.github.kshashov.timetracker.data.repo.user;

import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.data.repo.BaseRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface RolesRepository extends JpaRepository<Role, Long>, BaseRepo {
    Role findOneByTitle(String title);

    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions")
    Set<Role> findAllWithPermissions();
}
