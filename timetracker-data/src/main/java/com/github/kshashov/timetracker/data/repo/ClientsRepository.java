package com.github.kshashov.timetracker.data.repo;

import com.github.kshashov.timetracker.data.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientsRepository extends JpaRepository<Client, Long> {
}
