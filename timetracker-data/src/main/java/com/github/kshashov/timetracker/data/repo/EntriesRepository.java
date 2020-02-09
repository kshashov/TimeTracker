package com.github.kshashov.timetracker.data.repo;

import com.github.kshashov.timetracker.data.entity.Entry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntriesRepository extends JpaRepository<Entry, Long>, BaseRepo {
}
