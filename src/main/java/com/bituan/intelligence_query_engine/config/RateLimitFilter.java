package com.bituan.intelligence_query_engine.config;

import com.bituan.intelligence_query_engine.exception.TooManyRequests;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket resolveBucket(String key, boolean isAuth) {
        return buckets.computeIfAbsent(key, k -> {
            int capacity = isAuth ? 10 : 60;
            return Bucket.builder()
                    .addLimit(Bandwidth.classic(capacity,
                            Refill.greedy(capacity, Duration.ofMinutes(1))))
                    .build();
        });
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String userId = request.getRemoteAddr(); // IP
        boolean isAuth = path.startsWith("/auth/");

        String bucketKey = userId + ":" + (isAuth ? "auth" : "api");
        Bucket bucket = resolveBucket(bucketKey, isAuth);

        try {
            if (bucket.tryConsume(1)) {
                filterChain.doFilter(request, response);
            } else {
                throw new TooManyRequests("Too many Requests");
            }
        } catch (TooManyRequests e) {
            resolver.resolveException(request, response, null, e);
        }
    }
}