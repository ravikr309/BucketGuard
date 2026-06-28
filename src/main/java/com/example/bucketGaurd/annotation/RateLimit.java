package com.example.bucketGaurd.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.example.bucketGaurd.strategy.RateLimitStrategy;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
  String key() default "";

  long capacity() default 10;

  long refillRate() default 1;

  RateLimitStrategy strategy() default RateLimitStrategy.USER;

}
