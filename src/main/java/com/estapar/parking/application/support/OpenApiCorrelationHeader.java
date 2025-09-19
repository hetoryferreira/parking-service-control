package com.estapar.parking.application.support;

import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiCorrelationHeader {
    @Bean
    public OpenApiCustomizer correlationHeaderCustomiser() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(op ->
                        op.addParametersItem(new Parameter()
                                .in("header")
                                .name("X-Correlation-Id")
                                .required(false)
                                .description("Correlation ID propagated across services")
                                .schema(new StringSchema()))
                )
        );
    }
}