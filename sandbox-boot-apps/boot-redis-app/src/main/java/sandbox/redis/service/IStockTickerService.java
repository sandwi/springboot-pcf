package sandbox.redis.service;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import sandbox.redis.model.StockPrice;

public interface IStockTickerService {
    @Cacheable(value= "stockTickerCache", key= "#stockTicker", unless = "#result == null")
    Double getStockPrice(String stockTicker);

    @Caching(
            put= { @CachePut(value= "stockTickerCache", key= "#ticker") }
    )
    void addStockTicker(StockPrice stockPrice);
}
