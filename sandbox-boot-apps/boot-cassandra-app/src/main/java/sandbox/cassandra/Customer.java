package sandbox.cassandra;

import lombok.Data;
import lombok.NonNull;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;


import java.util.UUID;

@Data
@Table
public class Customer {
    @PrimaryKeyColumn(name = "id", ordinal = 0, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    @NonNull
    private UUID id;
    @PrimaryKeyColumn(name = "firstName", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    @NonNull
    private String firstName;
    @PrimaryKeyColumn(name = "lastName", ordinal = 2, type = PrimaryKeyType.PARTITIONED)
    @NonNull
    private String lastName;
    @Column
    @NonNull
    private String customerType;

}
