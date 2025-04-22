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

    /**
     * Creates and provides a configured instance of the AWS Secrets Manager client.
     * This client is used to interact with AWS Secrets Manager to retrieve secrets.
     *
     * @return a configured instance of SecretsManagerClient
     */
    @Bean
    public SecretsManagerClient secretsManagerClient() {
        return SecretsManagerClient.builder()
                .region(Region.of(region))
                .build();
    }

    /**
     * Retrieves a secret from AWS Secrets Manager and returns it as a String.
     *
     * This method uses the AWS SDK to access the Secrets Manager service and fetch
     * the secret value, which can be used for secure configurations such as database
     * credentials or API keys.
     *
     * @return the secret value retrieved from AWS Secrets Manager
     */
    @Bean
    public String getSecret() {
        SecretsManagerClient secretsManagerClient = secretsManagerClient();

        try {
            GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();

            GetSecretValueResponse valueResponse = secretsManagerClient.getSecretValue(valueRequest);

            if (valueResponse.secretString() != null) {
                logger.info("Successfully retrieved the secret from AWS Secrets Manager.");
                return valueResponse.secretString();
            } else {
                logger.warn("The secret retrieved is in binary format. You may need to handle binary secrets.");
                return null;
            }

        } catch (SecretsManagerException e) {
            logger.error("Error retrieving secret from AWS Secrets Manager: ", e);
            throw new RuntimeException("Error retrieving secret from AWS Secrets Manager", e);
        }
    }
}
