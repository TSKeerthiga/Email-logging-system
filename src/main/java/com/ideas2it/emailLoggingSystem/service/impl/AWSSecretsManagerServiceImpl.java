package com.ideas2it.emailLoggingSystem.service.impl;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ideas2it.emailLoggingSystem.service.AWSSecretsManagerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AWSSecretsManagerServiceImpl implements AWSSecretsManagerService  {

    private final String secretName;  // The name of your secret in Secrets Manager

    private final String region;

    public AWSSecretsManagerServiceImpl(@Value("${aws.secretName}") String secretName, @Value("${aws.region}") String region) {
        this.secretName = secretName;
        this.region = region;
    }

    /**
     * Retrieves AWS credentials from AWS Secrets Manager.
     *
     * This method fetches the secret from AWS Secrets Manager, parses the JSON response,
     * and extracts the AWS access key and secret key. These credentials are then returned
     * as a {@link BasicAWSCredentials} object, which can be used for AWS SDK interactions.
     *
     * @return {@link BasicAWSCredentials} containing the access key and secret key
     * @throws RuntimeException if there is an error retrieving or parsing the secret from AWS Secrets Manager,
     *                          or if the access key or secret key is missing in the retrieved secret
     */
    public BasicAWSCredentials getAWSCredentialsFromSecret() {
        // Initialize the Secrets Manager client
        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                .withRegion(region) // <-- this is key!
                .build();

        // Create a request to retrieve the secret
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);
        GetSecretValueResult secretValueResult = client.getSecretValue(getSecretValueRequest);

        String secretString = secretValueResult.getSecretString();

        // Parse the secret string (assuming it's a JSON object)
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(secretString);
            String accessKey = jsonNode.get("access-key-id").asText();
            String secretKey = jsonNode.get("secret-key").asText();
            System.out.println("Fetching secret: " + secretKey);
            System.out.println("Retrieved secret string: " + accessKey);
            // Return the credentials for further use
            return new BasicAWSCredentials(accessKey, secretKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse the secret: " + secretString, e);
        }
    }
}

