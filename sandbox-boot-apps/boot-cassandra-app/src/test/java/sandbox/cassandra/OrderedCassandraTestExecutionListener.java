package sandbox.cassandra;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cassandraunit.spring.CassandraUnitDependencyInjectionTestExecutionListener;
import org.springframework.core.Ordered;

public class OrderedCassandraTestExecutionListener
        extends CassandraUnitDependencyInjectionTestExecutionListener {

    private static final Log logger = LogFactory
            .getLog(OrderedCassandraTestExecutionListener.class);

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    protected void cleanServer() {
        try {
            super.cleanServer();
        }
        catch (Exception ex) {
            logger.warn("Failure during server cleanup", ex);
        }
    }

}
