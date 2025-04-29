package kr.hhplus.be.server.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public abstract class IntegrationTest {

    @Autowired
    DatabaseCleanup databaseCleanup;

    @BeforeEach
    void setUp() {
        databaseCleanup.execute();
    }
}
