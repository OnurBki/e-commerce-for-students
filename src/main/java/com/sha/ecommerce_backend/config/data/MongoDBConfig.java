package com.sha.ecommerce_backend.config.data;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Configuration
public class MongoDBConfig {
    private final MongoDBCustomProperties props;

    public MongoDBConfig(MongoDBCustomProperties props) {
        this.props = props;
    }

    @Bean
    public MongoClient mongoClient() {
        MongoCredential credential = MongoCredential.createCredential(
                props.getUsername(),
                props.getAuthenticationDatabase(),
                props.getPassword().toCharArray()
        );

        MongoClientSettings settings = MongoClientSettings.builder()
                .credential(credential)
                .applyToClusterSettings(builder ->
                        builder.hosts(Collections.singletonList(new ServerAddress(props.getHost(), props.getPort()))))
                .applyToConnectionPoolSettings(builder -> builder
                        .maxSize(25)
                        .minSize(5)
                        .maxWaitTime(10, TimeUnit.SECONDS)
                        .maxConnectionIdleTime(30, TimeUnit.SECONDS)
                )
                .build();

        return MongoClients.create(settings);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), props.getDatabase());
    }
}
