package com.wlt.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomHeaderFilter extends AbstractGatewayFilterFactory<CustomHeaderFilter.Config> {

    public CustomHeaderFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            exchange.getRequest().mutate()
                    .header("X-Request-Source", "API-Gateway")
                    .build();
            return chain.filter(exchange);
        };
    }

    public static class Config {
        // You can define configuration properties for your filter here if needed
    }
}