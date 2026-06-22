# BucketGuard
A high-performance, distributed rate-limiting library built for the Spring Boot ecosystem. Designed to protect microservices from cascading failures, brute-force attacks, and API abuse using a thread-safe, low-latency Token Bucket algorithm.


[ Incoming Request ]
         │
         ▼
 1. ┌────────────────────────────────────────────────────────┐
    │ annotation/RateLimited.java                            │
    │ (Finds the @RateLimited annotation on the controller)  │
    └────────────────────┬───────────────────────────────────┘
                         │
                         ▼
 2. ┌────────────────────────────────────────────────────────┐
    │ aspect/RateLimiterAspect.java                          │
    │ (Intercepts execution, reads limits from annotation)   │
    └────────────────────┬───────────────────────────────────┘
                         │
                         ▼
 3. ┌────────────────────────────────────────────────────────┐
    │ strategy/KeyResolver.java (e.g., IpKeyResolver)       │
    │ (Extracts client's IP or User ID to build unique key)  │
    └────────────────────┬───────────────────────────────────┘
                         │
                         ▼
 4. ┌────────────────────────────────────────────────────────┐
    │ core/RedisTokenBucketRateLimiter.java                  │
    │ (Asks Redis to evaluate token bucket for this key)     │
    └────────────────────┬───────────────────────────────────┘
                         │
        ┌────────────────┴────────────────┐
        │ [Tokens Available]              │ [Bucket Empty]
        ▼                                 ▼
 5A. ┌────────────────────────┐   5B. ┌────────────────────────────────┐
     │ Allow Request          │       │ exception/                     │
     │                        │       │ RateLimitExceededException     │
     │ (Injects HTTP headers; │       │ (Thrown up to the framework)   │
     │ passes to Controller)  │       └────────────────┬───────────────┘
     └────────────────────────┘                        │
                                                       ▼
                                  6.  ┌────────────────────────────────┐
                                      │ exception/                     │
                                      │ BucketGuardExceptionHandler    │
                                      │ (Catches error, returns a pure │
                                      │ HTTP 429 Too Many Requests)    │
                                      └────────────────────────────────┘
