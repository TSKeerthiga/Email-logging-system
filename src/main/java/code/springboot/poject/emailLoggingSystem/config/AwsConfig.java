package code.springboot.poject.emailLoggingSystem.config;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.regions.Region;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
    public class AwsConfig {

    @Bean
    public SecretsManagerClient secretsManagerClient() {
        return SecretsManagerClient.builder()
                .region(Region.US_EAST_1)  // Or the region you're using
                .build();
    }

    @Bean
    public String getSecret() {
        SecretsManagerClient secretsManagerClient = secretsManagerClient();

        // Correct usage of GetSecretValueRequest.builder()
        GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                .secretId("s3-email-cred")  // Replace with your secret's ID
                .build();

        // Fetch the secret value from AWS Secrets Manager
        GetSecretValueResponse valueResponse = secretsManagerClient.getSecretValue(valueRequest);
        return valueResponse.secretString(); // This is the secret value you want
    }
}
