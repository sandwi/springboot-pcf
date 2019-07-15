package sandbox.kafka.springkafka.cli;

public class BadStockTicker extends StockTicker {
    private final byte[] badMessage;

    public BadStockTicker(byte[] badMessage) {
        this.badMessage = badMessage;
    }

    public byte[] getBadMessage() {
        return this.badMessage;
    }
}
