package com.personal.aoppoc.config;

import com.personal.aoppoc.aspect.EndpointLoggingAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@ConditionalOnProperty(value = "endpoint.logging.enabled", havingValue = "true", matchIfMissing = true)
public class EndpointLoggingAutoConfiguration {

    @Bean
    public EndpointLoggingAspect endpointLoggingAspect() {
        return new EndpointLoggingAspect();
    }
}
