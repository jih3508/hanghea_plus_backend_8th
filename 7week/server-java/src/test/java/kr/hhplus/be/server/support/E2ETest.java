package kr.hhplus.be.server.support;

import kr.hhplus.be.server.ServerApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootTest(
    classes = ServerApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Sql(scripts = {"/sql/init.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class E2ETest {
    
    @Autowired
    protected MockMvc mockMvc;
    
    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected DatabaseCleanup databaseCleanup;

    @Autowired
    DataSource dataSource;

    @BeforeEach
    void setUp() {

//        databaseCleanup.execute();
//
//        try (Connection conn = dataSource.getConnection()) {
//            // 자신의 script path 넣어주면 됨
//            ScriptUtils.executeSqlScript(conn, new ClassPathResource("/sql/test-data.sql"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }
}
