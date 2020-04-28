package com.github.kshashov.timetracker.data.repo;

import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Project;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActionsRepository extends JpaRepository<Action, Long>, BaseRepo {
    boolean existsByProject(Project project);

    boolean existsByProjectAndTitle(Project project, String title);

    boolean existsByProjectAndTitleAndIdNot(Project project, String title, Long actionId);

    @EntityGraph(value = "Action.project")
    Action findWithProjectById(Long actionId);

    Action findOneByProjectAndTitle(Project project, String title);

    @EntityGraph(value = "Action.project")
    List<Action> findWithProjectByProject(Project project);

    List<Action> findByProjectAndIsActive(Project project, Boolean isActive);
}
