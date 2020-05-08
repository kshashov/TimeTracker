package com.github.kshashov.timetracker.data.service.admin.days;

import com.github.kshashov.timetracker.core.errors.NoPermissionException;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.repo.ProjectsRepository;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Service
public class AuthorizedClosedDaysServiceImpl implements AuthorizedClosedDaysService {
    private final ClosedDaysService closedDaysService;
    private final ProjectsRepository projectsRepository;
    private final RolePermissionsHelper rolePermissionsHelper;

    @Autowired
    public AuthorizedClosedDaysServiceImpl(
            ClosedDaysService closedDaysService,
            ProjectsRepository projectsRepository,
            RolePermissionsHelper rolePermissionsHelper) {
        this.closedDaysService = closedDaysService;
        this.projectsRepository = projectsRepository;
        this.rolePermissionsHelper = rolePermissionsHelper;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public void openDays(@NotNull User user, @NotNull Long projectId, @NotNull LocalDate from, @NotNull LocalDate to) {
        Project project = projectsRepository.getOne(projectId);
        if (!rolePermissionsHelper.hasProjectPermission(user, project, ProjectPermissionType.VIEW_PROJECT_LOGS)) {
            throw new NoPermissionException("You have no permissions to update this project");
        }

        closedDaysService.openDays(projectId, from, to);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public void closeDays(@NotNull User user, @NotNull Long projectId, @NotNull LocalDate from, @NotNull LocalDate to) {
        Project project = projectsRepository.getOne(projectId);
        if (!rolePermissionsHelper.hasProjectPermission(user, project, ProjectPermissionType.VIEW_PROJECT_LOGS)) {
            throw new NoPermissionException("You have no permissions to update this project");
        }

        closedDaysService.closeDays(projectId, from, to);
    }
}
