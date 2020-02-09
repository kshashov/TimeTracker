package com.github.kshashov.timetracker.data.repo;

import com.github.kshashov.timetracker.data.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestsRepository extends JpaRepository<Test, Long>, BaseRepo {
}
