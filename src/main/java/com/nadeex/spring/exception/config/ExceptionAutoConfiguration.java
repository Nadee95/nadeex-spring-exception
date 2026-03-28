package com.nadeex.spring.exception.config;

import com.nadeex.spring.exception.handler.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for the nadeex exception handling library.
 *
 * <p>Automatically registers {@link GlobalExceptionHandler} as a bean when:</p>
 * <ul>
 *   <li>The application is a servlet-based web application</li>
 *   <li>No other {@code GlobalExceptionHandler} bean has been defined by the consuming app</li>
 * </ul>
 *
 * <p>To override, declare your own {@code @RestControllerAdvice} class in your application
 * context and this auto-configuration will back off.</p>
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ExceptionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}

