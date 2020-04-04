package com.github.kshashov.timetracker.data.repo.user;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.repo.BaseRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<User, Long>, BaseRepo {
    User findOneByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN ProjectRole pr ON (u.id = pr.user.id) AND (pr.project.id = :#{#project.id})" +
            "WHERE (pr = null) AND (u.name LIKE %:name%)")
    Page<User> findMissingProjectUsers(@Param("project") Project project, @Param("name") String name, Pageable offsetLimitRequest);
}