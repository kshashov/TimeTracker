package com.github.kshashov.timetracker.data.repo;

import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ActionsRepository extends JpaRepository<Action, Long>, BaseRepo {
    Set<Action> findAllByProject(Project project);

    @Query("SELECT COUNT(a) > 0 FROM Action a WHERE a.project.id = :projectId AND a.title = :title")
    boolean hasAction(@Param("projectId") Long projectId, @Param("title") String projectTitle);
}
