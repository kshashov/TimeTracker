package com.github.kshashov.timetracker.data;

import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.repo.ActionsRepository;
import lombok.Getter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BaseActionTest extends BaseProjectTest {
    Action action;

    @Autowired
    private ActionsRepository actionsRepository;

    @BeforeAll
    void prepareAction() {
        this.action = actionsRepository.findWithProjectById(0L);
    }
}
