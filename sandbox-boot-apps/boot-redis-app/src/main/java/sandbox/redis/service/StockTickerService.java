package sandbox.redis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import sandbox.redis.model.StockPrice;

import java.util.Map;

@Slf4j
public class StockTickerService implements IStockTickerService {

    private Map<String, Double> stockPrices = Map.of(
            "AAPL", 200.72,
            "AMZN", 1899.87,
            "FB", 188.65,
            "GOOG", 1162.38);

    public StockTickerService()  {}

    @Override
    @Cacheable(value= "stockTickerCache", key= "#stockTicker", unless = "#result == null")
    public Double getStockPrice(String stockTicker) {
        log.info("getStockPrice(), ticker: " + stockTicker + ", Price: " + stockPrices.get(stockTicker));
        return stockPrices.get(stockTicker);
    }

    @Override
    @Caching(
            put= { @CachePut(value= "stockTickerCache", key= "#ticker") }
    )
    public void addStockTicker(StockPrice stockPrice) {
        log.info("addStockTicker() - Ticker: " + stockPrice.getTicker() + ", Price: " + stockPrice.getPrice());
        stockPrices.put(stockPrice.getTicker(), stockPrice.getPrice());
    }
}
