package sandbox.cassandra;

import com.datastax.driver.core.utils.UUIDs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

@Slf4j
public class SandboxApplication implements CommandLineRunner {

    @Autowired
    private CustomerRepository repository;


    public static void main(String[] args) {
        SpringApplication.run(SandboxApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        this.repository.deleteAll();

        // save a couple of customers
        this.repository.save(new Customer(UUIDs.timeBased(), "James", "Bond", "PRIMARY"));
        this.repository.save(new Customer(UUIDs.timeBased(), "Jason", "Bourne", "JOINT_OWNER"));

        // fetch all customers
        log.info("Customers found with findAll():");
        for (Customer customer : this.repository.findAll()) {
            log.info(customer.toString());
        }

        // fetch an individual customer
        log.info("Customer found with findByFirstName('James'):");
        log.info(this.repository.findByFirstName("James").toString());

        log.info("Customers found with findByLastName('Bourne'):");
        for (Customer customer : this.repository.findByLastName("Bourne")) {
            log.info(customer.toString());
        }

        log.info("Customers found with findByCustomerType('PRIMARY'):");
        for (Customer customer : this.repository.findByLastName("PRIMARY")) {
            log.info(customer.toString());
        }
    }
}
