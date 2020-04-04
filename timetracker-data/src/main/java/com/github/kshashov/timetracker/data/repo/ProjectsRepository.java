package com.github.kshashov.timetracker.data.repo;

import com.github.kshashov.timetracker.data.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectsRepository extends JpaRepository<Project, Long>, BaseRepo {

    boolean existsByTitle(String title);

    @Query("SELECT COUNT(p) > 0 FROM Project p WHERE p.title = :title AND p.id <> :#{#project.id}")
    boolean existsOtherByTitle(@Param("title") String title, @Param("project") Project project);
}
