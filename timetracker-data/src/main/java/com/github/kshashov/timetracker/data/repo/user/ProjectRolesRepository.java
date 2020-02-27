package com.github.kshashov.timetracker.data.repo.user;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.ProjectRoleIdentity;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.repo.BaseRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ProjectRolesRepository extends JpaRepository<ProjectRole, ProjectRoleIdentity>, BaseRepo {

    ProjectRole findOneByUserAndProject(User user, Project project);

    @Query("SELECT pr FROM ProjectRole pr LEFT JOIN FETCH pr.project WHERE pr.permissionIdentity.userId = :userId")
    Set<ProjectRole> findUserProjectsWithRoles(@Param("userId") Long userId);

    @Query("SELECT pr FROM ProjectRole pr LEFT JOIN FETCH pr.user WHERE pr.permissionIdentity.projectId = :projectId")
    Set<ProjectRole> findProjectUsersWithRoles(@Param("projectId") Long projectId);

    @Query("SELECT COUNT(pr)>0 FROM ProjectRole pr WHERE pr.permissionIdentity.userId = :userId AND pr.permissionIdentity.projectId = :projectId")
    boolean hasProjectRole(@Param("userId") Long userId, @Param("projectId") Long projectId);
}
