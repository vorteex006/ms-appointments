package com.vitalpet.msappointments.config;

import feign.RequestInterceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class InternalFeignConfig {

    @Bean
    public RequestInterceptor internalApiKeyInterceptor(
            @Value("${internal.api.key}") String apiKey
    ) {
        return template ->
                template.header("X-Internal-Api-Key", apiKey);
    }
}
