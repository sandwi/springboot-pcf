package sandbox.cassandra;

import org.cassandraunit.spring.CassandraDataSet;
import org.cassandraunit.spring.EmbeddedCassandra;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;

import static org.assertj.core.api.Assertions.assertThat;

@TestExecutionListeners(mergeMode = MergeMode.MERGE_WITH_DEFAULTS,
        listeners = { OrderedCassandraTestExecutionListener.class })
@SpringBootTest
@CassandraDataSet(keyspace = "sandboxKeySpace", value = "ddl.cql")
@EmbeddedCassandra(timeout = 60000)
public class SandboxCassandraApplicationTests {
    @Test
    void testDefaultSettings() {

    }

}
