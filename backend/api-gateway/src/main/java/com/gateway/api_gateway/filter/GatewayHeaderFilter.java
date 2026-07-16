package com.gateway.api_gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class GatewayHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .flatMap(authentication -> {

                    String email = authentication.getName();
                    String role = authentication.getAuthorities()
                            .iterator()
                            .next()
                            .getAuthority();

                    @SuppressWarnings("unchecked")
                    Map<String, Object> details =
                            (Map<String, Object>) authentication.getDetails();

                    String userId = (String) details.get("id");
                    String name = (String) details.get("name");
                    ServerWebExchange mutatedExchange =
                            exchange.mutate()
                                    .request(request -> {
                                        request.header("X-User-Id", String.valueOf(userId));
                                        request.header("X-User-Email", email);
                                        request.header("X-User-Name", name);
                                        request.header("X-User-Role", role);
                                    })
                                    .build();

                    return chain.filter(mutatedExchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}