package code.springboot.poject.emailLoggingSystem.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AWSSecretsManagerService {

    private final String secretName;  // The name of your secret in Secrets Manager

    private final String region;

    public AWSSecretsManagerService(@Value("${aws.secretName}") String secretName, @Value("${aws.region}") String region) {
        this.secretName = secretName;
        this.region = region;
    }

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

