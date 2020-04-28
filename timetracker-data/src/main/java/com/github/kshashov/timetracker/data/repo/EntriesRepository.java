package com.github.kshashov.timetracker.data.repo;

import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Entry;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EntriesRepository extends JpaRepository<Entry, Long>, BaseRepo {
    boolean existsByAction(Action action);

    boolean existsByUserAndActionProject(User user, Project project);

    @EntityGraph("Entry.actionProject.user")
    Entry findWithUserAndProjectById(Long id);

    @EntityGraph("Entry.actionProject.user")
    List<Entry> findFullByUserAndObs(User user, LocalDate obs);

    List<Entry> findByActionProject(Project project);

    List<Entry> findByAction(Action project);

    @EntityGraph("Entry.actionProject.user")
    List<Entry> findByUserAndActionProject(User user, Project project);

    @Transactional(propagation = Propagation.REQUIRED)
    long deleteByActionAndIsClosed(Action action, boolean closed);

    @Transactional(propagation = Propagation.REQUIRED)
    void deleteByUserAndActionProjectAndIsClosed(User user, Project project, boolean closed);
}
