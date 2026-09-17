package com.equibalance.controller;

import com.equibalance.dto.PriceResponse;
import com.equibalance.service.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prices")
@RequiredArgsConstructor
public class PriceController {

    private final PriceService priceService;

    @GetMapping("/{ticker}")
    public PriceResponse getPrice(@PathVariable String ticker) {
        return priceService.getPrice(ticker);
    }
}
