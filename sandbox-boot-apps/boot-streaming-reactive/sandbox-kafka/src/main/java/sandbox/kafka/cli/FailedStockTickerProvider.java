package sandbox.kafka.cli;

import org.apache.kafka.common.header.Headers;

import java.util.function.BiFunction;

public class FailedStockTickerProvider implements BiFunction<byte[], Headers, StockTicker> {

    @Override
    public StockTicker apply(byte[] t, Headers u) {
        return new BadStockTicker(t);
    }
}
