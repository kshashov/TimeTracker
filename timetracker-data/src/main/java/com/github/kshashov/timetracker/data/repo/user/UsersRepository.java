package com.github.kshashov.timetracker.data.repo.user;

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

    @Query("SELECT u FROM User u WHERE u.name LIKE %:name%")
        // WHERE u.name LIKE %:name%
    Page<User> findAll(@Param("name") String name, Pageable offsetLimitRequest);
}
