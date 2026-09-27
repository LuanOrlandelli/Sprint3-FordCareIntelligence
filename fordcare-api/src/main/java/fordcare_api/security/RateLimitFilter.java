package fordcare_api.security;

import fordcare_api.service.AuditService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter
        extends OncePerRequestFilter {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    RateLimitFilter.class
            );

    private final Map<String, Bucket> buckets =
            new ConcurrentHashMap<>();

    private final AuditService auditService;

    public RateLimitFilter(
            AuditService auditService
    ) {
        this.auditService = auditService;
    }

    private Bucket createNewBucket() {

        Bandwidth limit =
                Bandwidth.classic(
                        20,
                        Refill.greedy(
                                20,
                                Duration.ofMinutes(1)
                        )
                );

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket resolveBucket(
            String clientIp
    ) {

        return buckets.computeIfAbsent(
                clientIp,
                key -> createNewBucket()
        );
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String clientIp =
                request.getRemoteAddr();

        Bucket bucket =
                resolveBucket(clientIp);

        if (bucket.tryConsume(1)) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String endpoint =
                request.getRequestURI();

        logger.warn(
                "RATE_LIMIT_EXCEEDED endpoint={} ip={}",
                endpoint,
                clientIp
        );

        auditService.register(
                "RATE_LIMIT_EXCEEDED",
                "UNKNOWN",
                endpoint,
                clientIp
        );

        response.setStatus(429);

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        response.getWriter().write("""
                {
                    "status": 429,
                    "error": "Too Many Requests",
                    "message": "Limite de requisições excedido. Tente novamente em alguns instantes."
                }
                """);
    }
}