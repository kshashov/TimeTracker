package com.github.kshashov.timetracker.data.repo.user;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.ProjectRoleIdentity;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.repo.BaseRepo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRolesRepository extends JpaRepository<ProjectRole, ProjectRoleIdentity>, BaseRepo {

    @Query("SELECT COUNT(pr) > 0 FROM ProjectRole pr WHERE pr.identity.userId = :#{#user.id} AND pr.identity.projectId = :#{#project.id}")
    boolean existsByProjectAndUser(@Param("project") Project project, @Param("user") User user);

    @EntityGraph(value = "ProjectRole.role")
    ProjectRole findOneByUserIdAndProjectId(@Param("userId") Long userId, @Param("projectId") Long projectId);

    @Query("SELECT pr FROM ProjectRole pr LEFT JOIN FETCH pr.project WHERE pr.identity.userId = :#{#user.id}")
    List<ProjectRole> findUserProjectsWithRoles(@Param("user") User user);

    @Query("SELECT pr FROM ProjectRole pr LEFT JOIN FETCH pr.user WHERE pr.identity.projectId = :#{#project.id}")
    List<ProjectRole> findProjectUsersWithRoles(@Param("project") Project project);

    @Query("SELECT COUNT(pr)>0 FROM ProjectRole pr WHERE pr.identity.userId = :#{#user.id} AND pr.identity.projectId = :#{#project.id}")
    boolean hasProjectRole(@Param("user") User user, @Param("project") Project project);

    long deleteByProject(@Param("project") Project project);

    List<ProjectRole> findAllByProject(Project project);
}
