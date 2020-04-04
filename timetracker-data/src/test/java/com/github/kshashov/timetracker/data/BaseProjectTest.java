package com.github.kshashov.timetracker.data;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.repo.ProjectsRepository;
import com.github.kshashov.timetracker.data.repo.user.UsersRepository;
import com.github.kshashov.timetracker.data.service.admin.projects.ProjectsService;
import lombok.Getter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BaseProjectTest extends BaseUserTest {
    Project project;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ProjectsService projectsService;

    @Autowired
    private ProjectsRepository projectsRepository;

    @BeforeAll
    void prepareProject() {
        this.project = projectsRepository.findById(1L).get();
    }
}
