package sandbox.redis.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import sandbox.redis.model.StockPrice;
import sandbox.redis.service.StockTickerService;

@RestController
@Slf4j
public class RedisController {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    StockTickerService stockTickerService;

    @GetMapping(value = "/stock/{ticker}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public Double getStockPrice(@PathVariable("ticker") String ticker) {
        log.info("getStockPrice(), ticker: " + ticker);
        return stockTickerService.getStockPrice(ticker);
    }

    @PostMapping(value = "/stock")
    public ResponseEntity<Void> addStockTicker(StockPrice stockPrice, UriComponentsBuilder builder) {
        stockTickerService.addStockTicker(stockPrice);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/stock/{ticker}").buildAndExpand(stockPrice.getTicker()).toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }
}
