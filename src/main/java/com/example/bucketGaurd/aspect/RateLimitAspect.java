package com.example.bucketGaurd.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.bucketGaurd.annotation.RateLimit;
import com.example.bucketGaurd.core.TokenBucketEngine;

import com.example.bucketGaurd.strategy.StrategyResolver;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class RateLimitAspect {

  private final TokenBucketEngine tokenBucketEngine;
  private final StrategyResolver strategyResolver;

  public RateLimitAspect(TokenBucketEngine tokenBucketEngine, StrategyResolver strategyResolver) {
    this.tokenBucketEngine = tokenBucketEngine;
    this.strategyResolver = strategyResolver;
  }

  @Before("@annotation(rateLimit)")
  public void interceptRateLimit(RateLimit rateLimit) {
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attributes == null)
      return;

    HttpServletRequest request = attributes.getRequest();

    // 1. Resolve strategy unique key
    String strategyKey = strategyResolver.resolveStrategy(rateLimit.strategy(), request);
    String finalKey = rateLimit.key().isEmpty() ? strategyKey : rateLimit.key() + ":" + strategyKey;

    // 2. Evaluate against Token Bucket
    boolean allowed = tokenBucketEngine.isAllowed(
        finalKey,
        rateLimit.capacity(),
        rateLimit.refillRate());

    // 3. Deny access if limit exceeded
    if (!allowed) {
      throw new RuntimeException("Too many requests. Please slow down.");
    }
  }

}
