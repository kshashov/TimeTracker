package com.github.kshashov.timetracker.data;

import com.github.kshashov.timetracker.data.repo.TestsRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

@DataJpaTest
public class TestJPA extends BaseTest {
    @Autowired
    TestsRepository testsRepository;

    @Test
    void testJpa() {
        List<com.github.kshashov.timetracker.data.entity.Test> tests = testsRepository.findAll();
        Assertions.assertNotNull(tests);
        Assertions.assertEquals(1, tests.size());
        Assertions.assertEquals("text", tests.get(0).getTitle());
    }
}
