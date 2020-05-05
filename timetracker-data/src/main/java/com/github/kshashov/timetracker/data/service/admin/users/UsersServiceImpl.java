package com.github.kshashov.timetracker.data.service.admin.users;

import com.github.kshashov.timetracker.core.errors.IncorrectArgumentException;
import com.github.kshashov.timetracker.core.errors.NoPermissionException;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.repo.user.UsersRepository;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.util.Objects;

@Service
public class UsersServiceImpl implements UsersService {

    private final UsersRepository usersRepository;

    @Autowired
    public UsersServiceImpl(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public User getOrCreateUser(String email, String name) {
        User user = usersRepository.findOneByEmail(email);

        if (user == null) {
            try {
                return createUser(email, name);
            } catch (javax.validation.ConstraintViolationException ex) {
                throw new IncorrectArgumentException("Invalid request", ex);
            } catch (DataIntegrityViolationException ex) {
                if (ex.getCause() instanceof ConstraintViolationException) {
                    ConstraintViolationException casted = (ConstraintViolationException) ex.getCause();
                    if ("users_unique_email".equals(casted.getConstraintName())) {
                        throw new IncorrectArgumentException("User " + email + " already exists", ex);
                    } else {
                        throw new IncorrectArgumentException("Invalid request", ex);
                    }
                }
                throw ex;
            }
        }

        return user;
    }

    private User createUser(String email, String name) {
        // Check email only
        if (StringUtils.isBlank(email)) {
            throw new IncorrectArgumentException("Email is empty");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setWeekStart(DayOfWeek.MONDAY);
        user.setIsValidated(false);

        return usersRepository.save(user);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public User updateUser(@NotNull User user, @NotNull User updatedUser) {
        Objects.requireNonNull(updatedUser.getId());

        // TODO check premissions
        // TODO check if email or validated is changed

        if (!user.getId().equals(updatedUser.getId())) {
            throw new NoPermissionException("User can be updated by the same user only");
        }

        return updateUser(user);
    }

    @Override
    public User updateUser(@NotNull User updatedUser) {
        // Validate
        Objects.requireNonNull(updatedUser.getId());
        preValidate(updatedUser);

        return usersRepository.save(updatedUser);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public boolean validate(@NotNull User user) {
        Objects.requireNonNull(user.getId());

        boolean validated = true;

        // TODO ignore other fields

        // Check name only
        if (StringUtils.isBlank(user.getName())) {
            validated = false;
        }

        if (validated) {
            user.setIsValidated(true);
            return usersRepository.save(user).getIsValidated();
        }

        return false;
    }

    private void preValidate(User user) {
        if (StringUtils.isBlank(user.getEmail())) {
            throw new IncorrectArgumentException("Email is empty");
        }

        if (StringUtils.isBlank(user.getName())) {
            throw new IncorrectArgumentException("Name is empty");
        }

        if (user.getWeekStart() == null) {
            throw new IncorrectArgumentException("Week starting day is empty");
        }
    }
}
