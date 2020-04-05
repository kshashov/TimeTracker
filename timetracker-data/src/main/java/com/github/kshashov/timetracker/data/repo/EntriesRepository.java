package com.github.kshashov.timetracker.data.repo;

import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Entry;
import com.github.kshashov.timetracker.data.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntriesRepository extends JpaRepository<Entry, Long>, BaseRepo {
    @Query("SELECT e FROM Entry e WHERE e.action.project.id = :#{#project.id}")
    List<Entry> findAllByProject(@Param("project") Project project);

    List<Entry> findAllByAction(Action project);

    List<Entry> findAllByUserIdAndActionProjectId(Long userId, Long projectId);

    boolean existsByAction(Action action);

    boolean existsByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndActionProjectId(@Param("userId") Long userId, @Param("projectId") Long projectId);

    long deleteByActionAndIsClosed(@Param("action") Action action, @Param("isClosed") boolean closed);

    void deleteByUserIdAndIsClosed(@Param("userId") Long userId, @Param("isClosed") boolean closed);

    void deleteByUserIdAndActionProjectIdAndIsClosed(@Param("userId") Long userId, @Param("projectId") Long projectId, @Param("isClosed") boolean closed);
}
