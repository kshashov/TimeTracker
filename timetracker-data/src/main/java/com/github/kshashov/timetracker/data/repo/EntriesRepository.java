package com.github.kshashov.timetracker.data.repo;

import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Entry;
import com.github.kshashov.timetracker.data.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntriesRepository extends JpaRepository<Entry, Long>, BaseRepo {
    boolean existsByAction(Action action);

    boolean existsByUserIdAndActionProjectId(Long userId, Long projectId);

    List<Entry> findByActionProject(Project project);

    List<Entry> findByAction(Action project);

    List<Entry> findByUserIdAndActionProjectId(Long userId, Long projectId);

    long deleteByActionAndIsClosed(Action action, boolean closed);

    void deleteByUserIdAndActionProjectIdAndIsClosed(Long userId, Long projectId, boolean closed);
}
