package com.github.kshashov.timetracker.data.repo.user;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.Permission;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.ProjectRoleIdentity;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.repo.BaseRepo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProjectRolesRepository extends JpaRepository<ProjectRole, ProjectRoleIdentity>, BaseRepo {

    boolean existsByUserAndProject(User user, Project project);

    boolean existsByUserIdAndProjectId(Long userId, Long projectId);

    ProjectRole findOneByUserIdAndProjectId(Long userId, Long projectId);

    @EntityGraph(value = "ProjectRole.project.user")
    ProjectRole findFullByIdentity(ProjectRoleIdentity id);

    @EntityGraph(value = "ProjectRole.project")
    List<ProjectRole> findWithProjectByUser(User user);

    @EntityGraph(value = "ProjectRole.projectActions")
    List<ProjectRole> findWithActionsByUserAndRolePermissionsContains(User user, Permission permission);

    @EntityGraph(value = "ProjectRole.project")
    List<ProjectRole> findWithProjectByUserAndRoleCodeNot(User user, String roleCode);

    @EntityGraph(value = "ProjectRole.user")
    List<ProjectRole> findWithUserByProjectAndRoleCodeNot(Project project, String roleCode);

    @EntityGraph(value = "ProjectRole.user")
    List<ProjectRole> findWithUserByProject(Project project);

    @Transactional(propagation = Propagation.REQUIRED)
    long deleteByProject(Project project);
}
