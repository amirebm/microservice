package com.microservice.order_service.controller;

import com.microservice.order_service.dto.OrderRequest;
import com.microservice.order_service.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@CircuitBreaker(name="inventory", fallbackMethod = "fallBackMethod")
@TimeLimiter(name="inventory")
@Retry(name = "inventory")

public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompletableFuture<String> placeOrder(@RequestBody OrderRequest orderRequest) throws IllegalAccessException {
        return CompletableFuture.supplyAsync(()-> {
            try {
                return orderService.placeOrder(orderRequest);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }) ;

    }

    public CompletableFuture<String> fallBackMethod(OrderRequest orderRequest,RuntimeException runtimeException){
        return CompletableFuture.supplyAsync(()->"Oops, Something goes wrong Amir" ) ;
    }
}
