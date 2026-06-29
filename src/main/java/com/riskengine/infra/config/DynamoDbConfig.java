package com.riskengine.infra.config;


import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

@Factory
public class DynamoDbConfig {

    @Singleton
    public DynamoDbClient dynamoDbClient(
            @Value("${riskengine.dynamodb.endpoint}") String endpoint,
            @Value("${riskengine.dynamodb.region}") String region
    ) {
        return DynamoDbClient.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("local", "local")
                        )
                )
                .build();
    }
}