package sandbox;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@org.springframework.stereotype.Service
public class CustomerService implements Service {
    @Value("${sandbox.customer.service.name:unknown}")
    private String name;

    @Override
    public String getName() {
        return name;
    }
}
