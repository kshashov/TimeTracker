package com.github.kshashov.timetracker.data.repo;

import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Entry;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.Permission;
import com.github.kshashov.timetracker.data.entity.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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
    List<Entry> findFullByUserAndObsOrderByActionProjectTitleAscActionTitleAscCreatedAtAsc(User user, LocalDate obs);

    List<Entry> findByActionProject(Project project);

    List<Entry> findByAction(Action project);

    @EntityGraph("Entry.actionProject.user")
    List<Entry> findFullByUserAndActionProject(User user, Project project);

    @Query("SELECT COUNT(e) FROM Entry e LEFT JOIN ProjectRole pr " +
            "ON (e.action.project.id = pr.project.id) AND (e.user.id = pr.user.id)" +
            "WHERE (e.action.project.id = :#{#project.id}) AND (:#{#permission} member pr.role.permissions)" +
            "AND (e.obs BETWEEN :#{#from} AND :#{#to}) " +
            "AND (e.action.project.isActive = true) AND (e.action.isActive = true)")
    long countByProjectAndUserPermission(Project project, Permission permission, LocalDate from, LocalDate to);

    @Query("SELECT COUNT(e) FROM Entry e LEFT JOIN ProjectRole pr " +
            "ON (e.action.project.id = pr.project.id) AND (e.user.id = pr.user.id)" +
            "WHERE (e.action.project.id = :#{#project.id}) AND (:#{#permission} member pr.role.permissions)" +
            "AND (e.obs BETWEEN :#{#from} AND :#{#to}) AND (e.isClosed = true)" +
            "AND (e.action.project.isActive = true) AND (e.action.isActive = true)")
    long countClosedByProjectAndUserPermission(Project project, Permission permission, LocalDate from, LocalDate to);

    @EntityGraph(value = "Entry.actionProject.user")
    @Query("SELECT e FROM Entry e LEFT JOIN ProjectRole pr " +
            "ON (e.action.project.id = pr.project.id) AND (pr.user.id = :#{#user.id})" +
            "WHERE ((e.user.id = :#{#user.id}) OR (:#{#permission} member pr.role.permissions))" +
            "AND (e.obs BETWEEN :#{#from} AND :#{#to})" +
            "ORDER BY e.user.name, e.action.project.title, e.action.title, e.obs, e.hours, e.createdAt ASC")
    List<Entry> findFullByUserAndReporterUserPermission(User user, Permission permission, LocalDate from, LocalDate to);

    @Query("SELECT new com.github.kshashov.timetracker.data.repo.EntriesStats(e.action.project.title, SUM(e.hours)) " +
            "FROM Entry e " +
            "WHERE (e.user.id = :#{#user.id}) AND (e.obs BETWEEN :#{#from} AND :#{#to})" +
            "GROUP BY e.action.project.title " +
            "ORDER BY SUM(e.hours), e.action.project.title")
    List<EntriesStats> statsByUser(User user, LocalDate from, LocalDate to);

    @Modifying
    @Transactional(propagation = Propagation.REQUIRED)
    @Query("UPDATE Entry e SET e.isClosed = false WHERE e.id IN (" +
            "SELECT e.id FROM Entry e LEFT JOIN ProjectRole pr " +
            "ON (e.action.project.id = pr.project.id) AND (e.user.id = pr.user.id)" +
            "WHERE (e.action.project.id = :#{#project.id}) AND (:#{#permission} member pr.role.permissions)" +
            "AND (e.obs BETWEEN :#{#from} AND :#{#to}) " +
            "AND (e.action.project.isActive = true) AND (e.action.isActive = true))")
    int openByProjectAndUserPermission(Project project, Permission permission, LocalDate from, LocalDate to);

    @Modifying
    @Transactional(propagation = Propagation.REQUIRED)
    @Query("UPDATE Entry e SET e.isClosed = true WHERE e.id IN (" +
            "SELECT e.id FROM Entry e LEFT JOIN ProjectRole pr " +
            "ON (e.action.project.id = pr.project.id) AND (e.user.id = pr.user.id)" +
            "WHERE (e.action.project.id = :#{#project.id}) AND (:#{#permission} member pr.role.permissions)" +
            "AND (e.obs BETWEEN :#{#from} AND :#{#to}) " +
            "AND (e.action.project.isActive = true) AND (e.action.isActive = true))")
    int closeByProjectAndUserPermission(Project project, Permission permission, LocalDate from, LocalDate to);

    @Transactional(propagation = Propagation.REQUIRED)
    long deleteByActionAndIsClosed(Action action, boolean closed);

    @Transactional(propagation = Propagation.REQUIRED)
    void deleteByUserAndActionProjectAndIsClosed(User user, Project project, boolean closed);
}
