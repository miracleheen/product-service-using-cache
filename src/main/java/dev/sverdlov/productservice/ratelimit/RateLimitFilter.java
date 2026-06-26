package dev.sverdlov.productservice.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final FixedWindowRateLimiter fixedWindowRateLimiter;

    @Value("${rate-limit.enabled}")
    private boolean enableRatelimiting;

    @Value("${rate-limit.limit}")
    private Integer rateLimitLimit;

    @Value("${rate-limit.window-size}")
    private Duration windowSize;

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (!enableRatelimiting) {
            filterChain.doFilter(req, response);
            return;
        }

        String client = Optional.ofNullable(req.getHeader("X-API-KEY"))
                .filter(s -> !s.isBlank())
                .orElseGet(() -> Optional.ofNullable(req.getRemoteAddr()).orElse("unknown"));

        boolean allowed = fixedWindowRateLimiter.allowRequest(
                client,
                rateLimitLimit,
                windowSize
        );

        if (!allowed) {
            response.setStatus(429);
            response.getWriter().write("Rate limit exceeded");
            return;
        }
        filterChain.doFilter(req, response);
    }
}
