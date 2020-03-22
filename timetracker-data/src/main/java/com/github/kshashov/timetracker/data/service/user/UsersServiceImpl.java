package com.github.kshashov.timetracker.data.service.user;

import com.github.kshashov.timetracker.core.errors.IncorrectArgumentException;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.repo.user.UsersRepository;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class UsersServiceImpl implements UsersService {

    private final UsersRepository usersRepository;

    @Autowired
    public UsersServiceImpl(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED)
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
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setIsValidated(false);
        return usersRepository.save(user);
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED)
    public boolean validate(User user) {
        if (!StringUtils.isBlank(user.getName())) {
            user.setIsValidated(true);
            return usersRepository.save(user).getIsValidated();
        }

        return false;
    }

}
