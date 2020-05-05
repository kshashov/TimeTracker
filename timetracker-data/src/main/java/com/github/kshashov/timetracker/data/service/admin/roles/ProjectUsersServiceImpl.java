package com.github.kshashov.timetracker.data.service.admin.roles;

import com.github.kshashov.timetracker.core.errors.IncorrectArgumentException;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.ProjectRoleIdentity;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.data.enums.ProjectRoleType;
import com.github.kshashov.timetracker.data.repo.EntriesRepository;
import com.github.kshashov.timetracker.data.repo.ProjectsRepository;
import com.github.kshashov.timetracker.data.repo.user.ProjectRolesRepository;
import com.github.kshashov.timetracker.data.repo.user.RolesRepository;
import com.github.kshashov.timetracker.data.repo.user.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;

@Service
public class ProjectUsersServiceImpl implements ProjectUsersService {
    private final ProjectRolesRepository projectRolesRepository;
    private final EntriesRepository entiesRepository;
    private final RolesRepository rolesRepository;
    private final ProjectsRepository projectsRepository;
    private final UsersRepository usersRepository;

    @Autowired
    public ProjectUsersServiceImpl(
            ProjectRolesRepository projectRolesRepository,
            EntriesRepository entiesRepository,
            RolesRepository rolesRepository,
            ProjectsRepository projectsRepository,
            UsersRepository usersRepository) {
        this.projectRolesRepository = projectRolesRepository;
        this.entiesRepository = entiesRepository;
        this.rolesRepository = rolesRepository;
        this.projectsRepository = projectsRepository;
        this.usersRepository = usersRepository;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public ProjectRole createProjectRole(@NotNull Long projectId, @NotNull Long userId, @NotNull ProjectRoleInfo projectRoleInfo) {
        return doCreateProjectRole(projectId, userId, projectRoleInfo);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public ProjectRole updateProjectRole(@NotNull ProjectRoleIdentity identity, @NotNull ProjectRoleInfo projectRoleInfo) {
        return doUpdateProjectRole(identity, projectRoleInfo);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public boolean deleteOrDeactivateProjectRole(@NotNull ProjectRoleIdentity projectRoleIdentity) {
        return doDeleteOrDeactivateProjectRole(projectRoleIdentity);
    }

    private ProjectRole doCreateProjectRole(@NotNull Long projectId, @NotNull Long userId, @NotNull ProjectRoleInfo projectRoleInfo) {
        preValidate(projectRoleInfo);

        Role role = rolesRepository.getOne(projectRoleInfo.getRoleId());

        // Validate
        if (projectRolesRepository.existsByUserIdAndProjectId(userId, projectId)) {
            throw new IncorrectArgumentException("Project user already exists");
        }

        // Create
        ProjectRole projectRole = new ProjectRole();
        var id = new ProjectRoleIdentity();
        id.setUserId(userId);
        id.setProjectId(projectId);
        projectRole.setProject(projectsRepository.getOne(projectId));
        projectRole.setUser(usersRepository.getOne(userId));
        projectRole.setRole(role);
        projectRole.setIdentity(id);
        projectRole = projectRolesRepository.save(projectRole);

        return projectRole;
    }

    public ProjectRole doUpdateProjectRole(@NotNull ProjectRoleIdentity identity, @NotNull ProjectRoleInfo projectRoleInfo) {
        preValidate(projectRoleInfo);

        ProjectRole projectRole = projectRolesRepository.findFullByIdentity(identity);
        Role role = rolesRepository.getOne(projectRoleInfo.getRoleId());

        // Update
        projectRole.setRole(role);
        projectRole = projectRolesRepository.save(projectRole);

        return projectRole;
    }

    private boolean doDeleteOrDeactivateProjectRole(@NotNull ProjectRoleIdentity identity) {
        ProjectRole projectRole = projectRolesRepository.findFullByIdentity(identity);
        if (ProjectRoleType.isInactive(projectRole.getRole())) {
            throw new IncorrectArgumentException("Project role is already inactive");
        }

        // Delete open entries with user
        entiesRepository.deleteByUserAndActionProjectAndIsClosed(projectRole.getUser(), projectRole.getProject(), false);

        //  Check if any entries are left
        if (entiesRepository.existsByUserAndActionProject(projectRole.getUser(), projectRole.getProject())) {
            // Deactivate project role
            Role role = rolesRepository.findOneByCode(ProjectRoleType.INACTIVE.getCode());
            projectRole.setRole(role);
            projectRolesRepository.save(projectRole);
            return false;
        }

        projectRolesRepository.deleteById(projectRole.getIdentity());
        return true;
    }

    private void preValidate(@NotNull ProjectRoleInfo projectRoleInfo) {
        if (projectRoleInfo.getRoleId() == null) {
            throw new IncorrectArgumentException("Project role is empty");
        }
    }
}
