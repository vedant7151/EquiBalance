package com.equibalance.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Thin client for Alpha Vantage's GLOBAL_QUOTE endpoint (free tier: 25 calls/day, 5/min).
 * Returns Optional.empty() on any failure so callers can fall back to the last cached price.
 */
@Slf4j
@Component
public class AlphaVantageClient {

    private final RestClient restClient;
    private final String apiKey;

    public AlphaVantageClient(@Value("${equibalance.price.alphavantage.base-url}") String baseUrl,
                               @Value("${equibalance.price.alphavantage.api-key}") String apiKey) {
        this.restClient = RestClient.create(baseUrl);
        this.apiKey = apiKey;
    }

    public Optional<BigDecimal> fetchPrice(String ticker) {
        try {
            String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("function", "GLOBAL_QUOTE")
                            .queryParam("symbol", ticker)
                            .queryParam("apikey", apiKey)
                            .build())
                    .retrieve()
                    .body(String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            JsonNode quote = root.get("Global Quote");

            if (quote == null || !quote.has("05. price")) {
                log.warn("Alpha Vantage returned no quote for {}: {}", ticker, response);
                return Optional.empty();
            }

            return Optional.of(new BigDecimal(quote.get("05. price").asText()));
        } catch (Exception e) {
            log.warn("Failed to fetch price for {} from Alpha Vantage: {}", ticker, e.getMessage());
            return Optional.empty();
        }
    }
}
