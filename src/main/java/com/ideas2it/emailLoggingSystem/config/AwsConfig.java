package com.ideas2it.emailLoggingSystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;
import software.amazon.awssdk.regions.Region;

@Configuration
public class AwsConfig {

    private static final Logger logger = LoggerFactory.getLogger(AwsConfig.class);

    @Value("${aws.secretName}")
    String secretName;

    @Value("${aws.region}")
    String region;

    @Bean
    public SecretsManagerClient secretsManagerClient() {
        return SecretsManagerClient.builder()
                .region(Region.of(region))  // Specify the region you're using
                .build();
    }

    @Bean
    public String getSecret() {
        SecretsManagerClient secretsManagerClient = secretsManagerClient();

        try {
            // Creating request to fetch the secret
            GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                    .secretId(secretName)  // Use the secret name
                    .build();

            // Fetch the secret value
            GetSecretValueResponse valueResponse = secretsManagerClient.getSecretValue(valueRequest);

            // Check if the response contains a string or binary secret
            if (valueResponse.secretString() != null) {
                logger.info("Successfully retrieved the secret from AWS Secrets Manager.");
                return valueResponse.secretString(); // Return the secret string
            } else {
                logger.warn("The secret retrieved is in binary format. You may need to handle binary secrets.");
                return null; // Handle binary secrets here if necessary
            }

        } catch (SecretsManagerException e) {
            logger.error("Error retrieving secret from AWS Secrets Manager: ", e);
            throw new RuntimeException("Error retrieving secret from AWS Secrets Manager", e);
        }
    }
}
