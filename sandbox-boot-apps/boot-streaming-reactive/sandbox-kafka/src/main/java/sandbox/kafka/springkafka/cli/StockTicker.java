package sandbox.kafka.springkafka.cli;

import lombok.Data;

@Data
public class StockTicker {
    private String ticker;
    private String price;
}
