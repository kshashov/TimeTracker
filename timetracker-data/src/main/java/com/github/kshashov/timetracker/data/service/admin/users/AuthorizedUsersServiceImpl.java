package com.github.kshashov.timetracker.data.service.admin.users;

import com.github.kshashov.timetracker.core.errors.NoPermissionException;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.repo.user.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;

@Service
public class AuthorizedUsersServiceImpl implements AuthorizedUsersService {

    private final UsersRepository usersRepository;
    private final UsersService usersService;

    @Autowired
    public AuthorizedUsersServiceImpl(UsersRepository usersRepository, UsersService usersService) {
        this.usersRepository = usersRepository;
        this.usersService = usersService;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public User updateUser(@NotNull User user, @NotNull Long userId, @NotNull UserInfo userInfo) {
        if (!user.getId().equals(userId)) {
            throw new NoPermissionException("User can be updated by the same user only");
        }

        return usersService.updateUser(userId, userInfo);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public boolean validate(@NotNull User user, @NotNull Long userId, @NotNull String name) {
        if (!user.getId().equals(userId)) {
            throw new NoPermissionException("User can be updated by the same user only");
        }

        return usersService.validate(userId, name);
    }
}
