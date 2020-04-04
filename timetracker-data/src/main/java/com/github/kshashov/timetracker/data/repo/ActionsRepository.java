package com.github.kshashov.timetracker.data.repo;

import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActionsRepository extends JpaRepository<Action, Long>, BaseRepo {
    //    @EntityGraph(value = "Action.project")
    List<Action> findAllByProject(Project project);

    boolean existsByProject(@Param("project") Project project);

    boolean existsByProjectAndTitle(@Param("project") Project project, @Param("title") String title);

    List<Action> findByProjectAndIsActive(@Param("project") Project project, @Param("isActive") Boolean isActive);

    @Query("SELECT COUNT(a) > 0 FROM Action a WHERE a.project.id = :#{#project.id} AND a.title = :title AND a.id <> :#{#action.id}")
    boolean existsOtherByProjectAndTitle(@Param("project") Project project, @Param("title") String title, @Param("action") Action action);
}
