package com.github.kshashov.timetracker.data.service;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.ProjectRoles;
import com.github.kshashov.timetracker.data.entity.user.ProjectRolesIdentity;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.repo.ProjectsRepository;
import com.github.kshashov.timetracker.data.repo.user.ProjectRolesRepository;
import com.github.kshashov.timetracker.data.repo.user.RolesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class ProjectsService {
    private ProjectsRepository projectsRepository;
    private ProjectRolesRepository projectRolesRepository;
    private RolesRepository rolesRepository;

    @Autowired
    public ProjectsService(ProjectsRepository projectsRepository, ProjectRolesRepository projectRolesRepository, RolesRepository rolesRepository) {
        this.projectsRepository = projectsRepository;
        this.projectRolesRepository = projectRolesRepository;
        this.rolesRepository = rolesRepository;
    }

    @Transactional
    public Project createProject(Project project, User user) {
        // TODO create and give creator permission
        // TODO validate project
        project.setIsActive(true);
        projectsRepository.save(project);

        ProjectRolesIdentity id = new ProjectRolesIdentity();
        id.setProjectId(project.getId());
        id.setUserId(user.getId());
        ProjectRoles role = new ProjectRoles();
        role.setPermissionIdentity(id);
        role.setRole(rolesRepository.findOneByTitle("admin"));
        projectRolesRepository.save(role);

        return project;
    }

    public Project saveProject(User user, Project project) {
        projectsRepository.save(project);
        return project;
    }

/*    public List<ProjectRoles> findProjectsWithRoles(User user) {
        // TODO find available for user
    }

    public List<ProjectRoles> findUsersWithRoles(Project user) {
        // TODO users with roles
    }*/
}
