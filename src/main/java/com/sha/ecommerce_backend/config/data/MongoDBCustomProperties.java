package com.sha.ecommerce_backend.config.data;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.data.mongodb")
@Data
public class MongoDBCustomProperties {
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private String authenticationDatabase;
}
