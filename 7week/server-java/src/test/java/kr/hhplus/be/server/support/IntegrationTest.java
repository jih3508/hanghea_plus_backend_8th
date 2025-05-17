package kr.hhplus.be.server.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
public abstract class IntegrationTest {

    @Autowired
    DatabaseCleanup databaseCleanup;

    @AfterEach
    void setUp() {
        databaseCleanup.execute();
    }
}
