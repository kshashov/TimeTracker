package com.github.kshashov.timetracker.data.service.admin.days;

import com.github.kshashov.timetracker.data.entity.ClosedDay;
import com.github.kshashov.timetracker.data.entity.ClosedDayIdentity;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.repo.ClosedDaysRepository;
import com.github.kshashov.timetracker.data.repo.ProjectsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Service
public class ClosedDaysServiceImpl implements ClosedDaysService {
    private final ClosedDaysRepository closedDaysRepository;
    private final ProjectsRepository projectsRepository;

    @Autowired
    public ClosedDaysServiceImpl(ClosedDaysRepository closedDaysRepository, ProjectsRepository projectsRepository) {
        this.closedDaysRepository = closedDaysRepository;
        this.projectsRepository = projectsRepository;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public void openDays(@NotNull Long projectId, @NotNull LocalDate from, @NotNull LocalDate to) {
        Project project = projectsRepository.getOne(projectId);
        closedDaysRepository.deleteByProjectAndIdentityObsBetween(project, from, to);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public void closeDays(@NotNull Long projectId, @NotNull LocalDate from, @NotNull LocalDate to) {
        Project project = projectsRepository.getOne(projectId);

        closedDaysRepository.deleteByProjectAndIdentityObsBetween(project, from, to);

        while (!from.isAfter(to)) {
            ClosedDayIdentity identity = new ClosedDayIdentity();
            identity.setProjectId(project.getId());
            identity.setObs(from);

            ClosedDay day = new ClosedDay();
            day.setIdentity(identity);

            closedDaysRepository.save(day);
            from = from.plusDays(1);
        }

    }
}
