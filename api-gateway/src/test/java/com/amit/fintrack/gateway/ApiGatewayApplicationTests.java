package com.amit.fintrack.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApiGatewayApplicationTests {

    @Test
    void applicationClassIsPresent() {
        assertNotNull(ApiGatewayApplication.class);
    }

    @Test
    void gatewayRoutesAreConfigured() {
        Properties properties = loadApplicationProperties();

        assertEquals(
                "auth-service-route",
                properties.getProperty("spring.cloud.gateway.server.webmvc.routes[0].id")
        );
        assertEquals(
                "${AUTH_SERVICE_URL:http://localhost:8081}",
                properties.getProperty("spring.cloud.gateway.server.webmvc.routes[0].uri")
        );
        assertEquals(
                "Path=/api/accounts/**",
                properties.getProperty("spring.cloud.gateway.server.webmvc.routes[1].predicates[0]")
        );
        assertEquals(
                "Path=/api/transactions/**",
                properties.getProperty("spring.cloud.gateway.server.webmvc.routes[2].predicates[0]")
        );
        assertEquals(
                "Path=/api/budgets/**",
                properties.getProperty("spring.cloud.gateway.server.webmvc.routes[3].predicates[0]")
        );
    }

    private static Properties loadApplicationProperties() {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new ClassPathResource("application.yaml"));
        Properties properties = factory.getObject();
        assertNotNull(properties);
        return properties;
    }

}
