package sandbox.kafka.cli;

import lombok.Data;

@Data
public class StockTicker {
    private String ticker;
    private String price;
}
