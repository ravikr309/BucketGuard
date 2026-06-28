package com.example.bucketGaurd.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bucketGaurd.annotation.RateLimit;
import com.example.bucketGaurd.strategy.RateLimitStrategy;

@RestController
@RequestMapping("/api")
public class TestController {

  // IP strategy: 5 max capacity, refills 1 token per second
  @GetMapping("/public-data")
  @RateLimit(strategy = RateLimitStrategy.IP_ADDRESS, capacity = 5, refillRate = 1)
  public String getPublicData() {
    return "Public data accessed successfully!";
  }

  // Secure strategy: Requires 'X-User-Id' header. 2 max capacity, refills 1 token
  // per second
  @GetMapping("/secure-data")
  @RateLimit(strategy = RateLimitStrategy.USER, capacity = 2, refillRate = 1)
  public String getSecureData() {
    return "Secure user data accessed successfully!";
  }
}