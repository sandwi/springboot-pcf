package sandbox.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends CassandraRepository<Customer, String> {

    @Query("Select * from customer where firstName=?0")
    Customer findByFirstName(String firstName);

    @Query("Select * from customer where lastName=?0")
    List<Customer> findByLastName(String lastName);

    @Query("select * from customer where customerType = ?0")
    List<Customer> findByCustomerType(String customerType);

}
