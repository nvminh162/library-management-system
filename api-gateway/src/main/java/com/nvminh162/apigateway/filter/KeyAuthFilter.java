package com.nvminh162.apigateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class KeyAuthFilter extends AbstractGatewayFilterFactory<KeyAuthFilter.Config> {
    
    @Value("${apiKey}")
    private String apiKey;
    
    static class Config {

    }

    public KeyAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!exchange.getRequest().getHeaders().containsKey("apiKey")) {
                throw new RuntimeException("Missing authorization information");
            }

            String key = exchange.getRequest().getHeaders().get("apiKey").get(0);

            System.out.println(key);
            System.out.println(apiKey);

            if (!key.equals(apiKey)) {
                throw new RuntimeException("Invalid API key token");
            }

            ServerHttpRequest request = exchange.getRequest();
            return chain.filter(exchange.mutate().request(request).build());
        };

    }


}
