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
    @Query("SELECT a FROM Action a JOIN FETCH a.project WHERE a.project.id = :projectId")
    List<Action> findAllByProject(@Param("projectId") Long project);

    boolean existsByProjectAndTitle(@Param("project") Project project, @Param("title") String title);

    @Query("SELECT COUNT(a) > 0 FROM Action a WHERE a.project.id = :projectId AND a.title = :title AND a.id <> :actionId")
    boolean existsOtherByProjectAndTitle(@Param("projectId") Long projectId, @Param("title") String title, @Param("actionId") Long actionId);
}
