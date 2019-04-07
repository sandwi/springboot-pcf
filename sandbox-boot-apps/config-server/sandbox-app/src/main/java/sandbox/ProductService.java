package sandbox;

import org.springframework.beans.factory.annotation.Value;

@org.springframework.stereotype.Service
public class ProductService implements Service {
    @Value("${sandbox.product.service.name:unknown}")
    private String name;

    @Override
    public String getName() {
        return name;
    }
}
