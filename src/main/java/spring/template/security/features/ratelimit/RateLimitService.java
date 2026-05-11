package spring.template.security.features.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Clock clock = Clock.systemUTC();

    @Value("${security.features.rate-limit.enabled:false}")
    private boolean enabled;

    @Value("${security.features.rate-limit.max-attempts:5}")
    private int maxAttempts;

    @Value("${security.features.rate-limit.window-ms:60000}")
    private long windowMs;

    public void check(String key) {
        if (!enabled) {
            return;
        }
        long now = clock.millis();
        Bucket bucket = buckets.compute(key, (ignored, current) -> {
            if (current == null || now - current.windowStartMs() > windowMs) {
                return new Bucket(now, 1);
            }
            return new Bucket(current.windowStartMs(), current.attempts() + 1);
        });
        if (bucket.attempts() > maxAttempts) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many authentication attempts");
        }
    }

    public void reset(String key) {
        buckets.remove(key);
    }

    private record Bucket(long windowStartMs, int attempts) {
    }
}
