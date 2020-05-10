package com.github.kshashov.timetracker.data.service.admin.projects;

import com.github.kshashov.timetracker.core.errors.IncorrectArgumentException;
import com.github.kshashov.timetracker.data.BaseUserTest;
import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Entry;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.repo.ActionsRepository;
import com.github.kshashov.timetracker.data.repo.EntriesRepository;
import com.github.kshashov.timetracker.data.repo.ProjectsRepository;
import com.github.kshashov.timetracker.data.repo.user.ProjectRolesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProjectsServiceTest extends BaseUserTest {
    @Autowired
    ProjectsService projectsService;
    @Autowired
    private ProjectsRepository projectsRepository;
    @Autowired
    private ProjectRolesRepository projectRolesRepository;
    @Autowired
    private EntriesRepository entriesRepository;
    @Autowired
    private ActionsRepository actionsRepository;

    @Test
    void createProject_Ok() {
        assertThat(projectsRepository.existsByTitle("createProject")).isFalse();

        ProjectInfo projectInfo = correctProjectInfo("createProject");

        // Create project
        Project result = projectsService.createProject(projectInfo);

        assertThat(projectsRepository.existsByTitle("createProject")).isTrue();
        result = projectsRepository.findById(result.getId()).get();
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getTitle()).isEqualTo("createProject");
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getActions()).isNull();
    }

    @Test
    void createProject_ProjectTitleAlreadyExist_IncorrectArgumentException() {
        // Create
        projectsService.createProject(correctProjectInfo("createProject_ProjectTitleAlreadyExist"));

        // Create again
        assertThatThrownBy(() -> projectsService.createProject(correctProjectInfo("createProject_ProjectTitleAlreadyExist")))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    @Test
    void createProject_IncorrectProject_IncorrectArgumentException() {
        ProjectInfo projectInfo = correctProjectInfo("");

        assertThatThrownBy(() -> projectsService.createProject(projectInfo))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    //
    // UPDATE
    //

    @Test
    void updateProject_ProjectTitleAlreadyExist_IncorrectArgumentException() {
        // Create first
        projectsService.createProject(correctProjectInfo("updateProject_ProjectTitleAlreadyExist_0"));
        // Create second
        Project result = projectsService.createProject(correctProjectInfo("updateProject_ProjectTitleAlreadyExist_1"));
        // Try to save with the same title
        ProjectInfo projectInfo = correctProjectInfo("updateProject_ProjectTitleAlreadyExist_0");

        assertThatThrownBy(() -> projectsService.updateProject(result.getId(), projectInfo))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    @Test
    void updateProject_IncorrectProject_IncorrectArgumentException() {
        // Empty title
        Project project = projectsService.createProject(correctProjectInfo("updateProject_IncorrectProject"));

        assertThatThrownBy(() -> projectsService.updateProject(project.getId(), correctProjectInfo("")))
                .isInstanceOf(IncorrectArgumentException.class);

        project.setIsActive(false);

        assertThatThrownBy(() -> projectsService.updateProject(project.getId(), correctProjectInfo("updateProject_IncorrectProject")))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    @Test
    void updateProject_Ok() {
        Project project = projectsService.createProject(correctProjectInfo("updateProject_0"));
        ProjectInfo projectInfo = correctProjectInfo("updateProject_1");

        projectsService.updateProject(project.getId(), projectInfo);
        Project result = projectsRepository.findById(project.getId()).orElse(null);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(project.getId());
        assertThat(result.getTitle()).isEqualTo("updateProject_1");
        assertThat(result.getActions()).isEqualTo(project.getActions());
    }

    //
    // ACTIVATE
    //

    @Test
    void activateProject_Ok() {
        // Prepare inactive project
        Project project = projectsService.createProject(correctProjectInfo("activateProject_IncorrectProject"));
        project.setIsActive(false);

        project = projectsRepository.findById(project.getId()).get();

        assertThat(project).isNotNull();
        assertThat(project.getIsActive()).isFalse();

        // Activate
        projectsService.activateProject(project.getId());

        project = projectsRepository.findById(project.getId()).get();

        assertThat(project).isNotNull();
        assertThat(project.getIsActive()).isTrue();
    }

    @Test
    void activateProject_IncorrectProject_IncorrectArgumentException() {
        // Active project
        Project project = projectsService.createProject(correctProjectInfo("activateProject_IncorrectProject"));
        assertThat(project).isNotNull();
        project.setIsActive(true);

        assertThatThrownBy(() -> projectsService.activateProject(project.getId()))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    //
    // DELETE
    //

    @Test
    void deleteOrDeactivateProject_IncorrectProject_IncorrectArgumentException() {
        // Inactive project
        Project project = projectsService.createProject(correctProjectInfo("updateProject_IncorrectProject"));
        project.setIsActive(false);

        assertThatThrownBy(() -> projectsService.deleteOrDeactivateProject(project.getId()))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    @Test
    @Sql("classpath:tests/ProjectsServiceTest.deleteOrDeactivateProject_NoActions.sql")
    void deleteOrDeactivateProject_NoActions_ReturnsTrue() {
        Project project = projectsRepository.findOneByTitle("deleteOrDeactivateProject_NoActions");

        // Project exists and has entries and actions
        assertThat(project).isNotNull();
        assertThat(projectRolesRepository.findWithUserByProjectOrderByUserName(project).size()).isEqualTo(1);
        assertThat(entriesRepository.findByActionProject(project).size()).isEqualTo(0);
        assertThat(actionsRepository.findWithProjectByProjectOrderByTitleAsc(project).size()).isEqualTo(0);

        boolean isDeleted = projectsService.deleteOrDeactivateProject(project.getId());

        // Project is deleted
        assertThat(isDeleted).isTrue();
        assertThat(projectsRepository.existsByTitle("deleteOrDeactivateProject_NoActions")).isFalse();
        assertThat(projectRolesRepository.findWithUserByProjectOrderByUserName(project).size()).isEqualTo(0);
        assertThat(entriesRepository.findByActionProject(project).size()).isEqualTo(0);
        assertThat(actionsRepository.findWithProjectByProjectOrderByTitleAsc(project).size()).isEqualTo(0);
    }

    @Test
    @Sql("classpath:tests/ProjectsServiceTest.deleteOrDeactivateProject_NoClosedEntries.sql")
    void deleteOrDeactivateProject_NoClosedEntries_ReturnsTrue() {
        Project project = projectsRepository.findOneByTitle("deleteOrDeactivateProject_NoClosedEntries");

        // Project exists and has entries and actions
        assertThat(project).isNotNull();
        assertThat(projectRolesRepository.findWithUserByProjectOrderByUserName(project).size()).isEqualTo(1);
        assertThat(entriesRepository.findByActionProject(project).size()).isEqualTo(2);
        assertThat(actionsRepository.findWithProjectByProjectOrderByTitleAsc(project).size()).isEqualTo(2);

        boolean isDeleted = projectsService.deleteOrDeactivateProject(project.getId());

        // Project is deleted
        assertThat(isDeleted).isTrue();
        assertThat(projectsRepository.existsByTitle("deleteOrDeactivateProject_NoClosedEntries")).isFalse();
        assertThat(projectRolesRepository.findWithUserByProjectOrderByUserName(project).size()).isEqualTo(0);
        assertThat(entriesRepository.findByActionProject(project).size()).isEqualTo(0);
        assertThat(actionsRepository.findWithProjectByProjectOrderByTitleAsc(project).size()).isEqualTo(0);
    }

    @Test
    @Sql("classpath:tests/ProjectsServiceTest.deleteOrDeactivateProject_HasClosedEntries.sql")
    void deleteOrDeactivateProject_HasClosedEntries_ReturnsFalse() {
        Project project = projectsRepository.findOneByTitle("deleteOrDeactivateProject_HasClosedEntries");

        // Project exists and has entries and actions
        assertThat(project).isNotNull();
        assertThat(projectRolesRepository.findWithUserByProjectOrderByUserName(project).size()).isEqualTo(1);
        assertThat(entriesRepository.findByActionProject(project).size()).isEqualTo(2);
        assertThat(actionsRepository.findWithProjectByProjectOrderByTitleAsc(project).size()).isEqualTo(2);

        boolean isDeleted = projectsService.deleteOrDeactivateProject(project.getId());

        // Project is deactivated
        assertThat(isDeleted).isFalse();
        assertThat(projectsRepository.existsByTitle("deleteOrDeactivateProject_HasClosedEntries")).isTrue();
        assertThat(projectRolesRepository.findWithUserByProjectOrderByUserName(project).size()).isEqualTo(1);
        assertThat(entriesRepository.findByActionProject(project).size()).isEqualTo(1);
        assertThat(entriesRepository.findByActionProject(project).stream()
                .allMatch(Entry::getIsClosed)
        ).isTrue();
        assertThat(actionsRepository.findWithProjectByProjectOrderByTitleAsc(project).size()).isEqualTo(1);
        assertThat(actionsRepository.findWithProjectByProjectOrderByTitleAsc(project).stream()
                .noneMatch(Action::getIsActive)
        ).isTrue();
    }

    ProjectInfo correctProjectInfo(String title) {
        ProjectInfo projectInfo = new ProjectInfo();
        projectInfo.setTitle(title);
        return projectInfo;
    }
}
