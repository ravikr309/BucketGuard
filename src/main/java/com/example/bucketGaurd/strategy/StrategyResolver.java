package com.example.bucketGaurd.strategy;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class StrategyResolver {

  public String resolveStrategy(RateLimitStrategy strategy, HttpServletRequest request) {
    String key;
    switch (strategy) {
      case USER:
        key = getUserId(request);
        break;
      case IP_ADDRESS:
        key = getIpAddress(request);
        break;
      default:
        throw new IllegalArgumentException("Unsupported strategy: " + strategy);
    }
    return key;
  }

  private String getIpAddress(HttpServletRequest request) {
    String xfHeader = request.getHeader("X-Forwarded-For");
    if (xfHeader != null && !xfHeader.isEmpty()) {
      return xfHeader.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

  private String getUserId(HttpServletRequest request) {
    String userId = request.getHeader("X-User-Id");
    if (userId == null || userId.isEmpty()) {
      throw new IllegalArgumentException("User ID missing from request headers");
    }
    return userId;
  }

}
