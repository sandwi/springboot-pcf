package sandbox.redis.model;

import lombok.Data;

@Data
public class StockPrice {
    private String ticker;
    private Double price;
}
