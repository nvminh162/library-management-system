package com.nvminh162.notificationservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * Cấu hình chung cho notification-service: WebClient và Circuit Breaker (Resilience4J).
 */
@Configuration
public class AppConfig {
    /**
     * Đăng ký WebClient.Builder dùng để gọi HTTP tới các service khác (ví dụ: user-service).
     * Có thể inject builder này vào service/controller và tùy chỉnh baseUrl, filters...
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    /**
     * Cấu hình mặc định cho Resilience4J Circuit Breaker.
     * Áp dụng cho mọi circuit breaker được tạo bởi Resilience4JCircuitBreakerFactory.
     *
     * <ul>
     *   <li><b>Time Limiter:</b> timeout 3 giây — gọi ngoài (HTTP, DB...) quá 3s sẽ bị coi là lỗi.</li>
     *   <li><b>Sliding window:</b> 10 — đánh giá dựa trên 10 lần gọi gần nhất.</li>
     *   <li><b>Sliding window type:</b> TIME_BASED — cửa sổ theo thời gian.</li>
     *   <li><b>Minimum calls:</b> 5 — cần ít nhất 5 lần gọi trong cửa sổ mới tính tỉ lệ lỗi.</li>
     *   <li><b>Failure rate threshold:</b> 50% — nếu &gt; 50% lỗi thì mở circuit (ngừng gọi, trả fallback).</li>
     * </ul>
     */
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(3))
                        .build())
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .slidingWindowSize(10)
                        .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                        .minimumNumberOfCalls(5)
                        .failureRateThreshold(50)
                        .build())
                .build());
    }
}