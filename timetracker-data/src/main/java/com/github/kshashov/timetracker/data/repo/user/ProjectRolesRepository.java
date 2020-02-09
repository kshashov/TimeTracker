package com.github.kshashov.timetracker.data.repo.user;

import com.github.kshashov.timetracker.data.entity.user.ProjectRoles;
import com.github.kshashov.timetracker.data.entity.user.ProjectRolesIdentity;
import com.github.kshashov.timetracker.data.repo.BaseRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ProjectRolesRepository extends JpaRepository<ProjectRoles, ProjectRolesIdentity>, BaseRepo {

    @Query("SELECT r FROM ProjectRoles r LEFT JOIN FETCH r.project WHERE r.permissionIdentity.userId = :userId")
    Set<ProjectRoles> findUserProjectsWithRoles(@Param("userId") Long userId);

    @Query("SELECT r FROM ProjectRoles r LEFT JOIN FETCH r.user WHERE r.permissionIdentity.projectId = :projectId")
    Set<ProjectRoles> findProjectUsersWithRoles(@Param("projectId") Long projectId);
}
