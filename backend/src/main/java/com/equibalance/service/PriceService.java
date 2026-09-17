package com.equibalance.service;

import com.equibalance.client.AlphaVantageClient;
import com.equibalance.dto.PriceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Live price lookups with a Redis cache in front of Alpha Vantage to respect its
 * 5 calls/min, 25 calls/day free-tier limit. Cache TTL is configurable
 * (equibalance.price.cache-ttl-seconds, default 60s per the PRD).
 *
 * On upstream failure, the last cached value (even if stale/expired from Redis's
 * perspective) cannot be recovered since Redis evicts on TTL - so a secondary
 * "last known good" key with no TTL is kept for the stale-badge fallback.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriceService {

    private static final String CACHE_PREFIX = "price:cache:";
    private static final String LAST_KNOWN_PREFIX = "price:last-known:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final AlphaVantageClient alphaVantageClient;

    @Value("${equibalance.price.cache-ttl-seconds}")
    private long cacheTtlSeconds;

    public PriceResponse getPrice(String ticker) {
        String symbol = ticker.toUpperCase();
        String cacheKey = CACHE_PREFIX + symbol;

        Map<String, Object> cached = readMap(cacheKey);
        if (cached != null) {
            return toResponse(symbol, cached, false);
        }

        Optional<BigDecimal> live = alphaVantageClient.fetchPrice(symbol);
        if (live.isPresent()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("price", live.get());
            entry.put("currency", "USD");
            entry.put("fetchedAt", Instant.now().toString());

            redisTemplate.opsForHash().putAll(cacheKey, entry);
            redisTemplate.expire(cacheKey, Duration.ofSeconds(cacheTtlSeconds));

            redisTemplate.opsForHash().putAll(LAST_KNOWN_PREFIX + symbol, entry);

            return toResponse(symbol, entry, false);
        }

        Map<String, Object> lastKnown = readMap(LAST_KNOWN_PREFIX + symbol);
        if (lastKnown != null) {
            log.warn("Serving stale price for {} - live fetch failed", symbol);
            return toResponse(symbol, lastKnown, true);
        }

        throw new IllegalStateException("No price available for ticker " + symbol
                + " (live fetch failed and no cached value exists)");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readMap(String key) {
        Map<Object, Object> raw = redisTemplate.opsForHash().entries(key);
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        raw.forEach((k, v) -> result.put(String.valueOf(k), v));
        return result;
    }

    private PriceResponse toResponse(String ticker, Map<String, Object> data, boolean stale) {
        return PriceResponse.builder()
                .ticker(ticker)
                .price(new BigDecimal(String.valueOf(data.get("price"))))
                .currency(String.valueOf(data.get("currency")))
                .fetchedAt(Instant.parse(String.valueOf(data.get("fetchedAt"))))
                .stale(stale)
                .build();
    }
}
