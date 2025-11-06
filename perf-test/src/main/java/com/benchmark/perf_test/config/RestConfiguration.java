package com.benchmark.perf_test.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class RestConfiguration implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        // Set base path for Spring Data REST to avoid conflicts with custom controllers
        config.setBasePath("/api/rest");

        // Return IDs in JSON responses
        config.exposeIdsFor(
            com.benchmark.perf_test.entity.Category.class,
            com.benchmark.perf_test.entity.Item.class
        );

        // Set default page size
        config.setDefaultPageSize(20);
        config.setMaxPageSize(100);
    }
}
