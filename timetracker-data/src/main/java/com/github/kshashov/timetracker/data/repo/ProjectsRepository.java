package com.github.kshashov.timetracker.data.repo;

import com.github.kshashov.timetracker.data.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectsRepository extends JpaRepository<Project, Long>, BaseRepo {
}
