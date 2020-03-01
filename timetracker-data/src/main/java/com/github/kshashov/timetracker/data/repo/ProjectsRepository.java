package com.github.kshashov.timetracker.data.repo;

import com.github.kshashov.timetracker.data.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectsRepository extends JpaRepository<Project, Long>, BaseRepo {

    @Query("SELECT COUNT(p) > 0 FROM Project p WHERE p.title = :title")
    boolean hasProject(@Param("title") String title);

    @Query("SELECT COUNT(p) > 0 FROM Project p WHERE p.title = :title AND p.id <> :projectId")
    boolean hasOtherProject(@Param("title") String title, @Param("projectId") Long projectId);
}
