package code.springboot.poject.emailLoggingSystem;

import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

public class TestAwsSecrets {
    @Value("${aws.bucketName}")
    private static String bucketName;

    public static void main(String[] args) {
        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(Region.US_EAST_1)
                .build();

        GetSecretValueRequest request = GetSecretValueRequest.builder()
                .secretId(bucketName)
                .build();

        GetSecretValueResponse response = client.getSecretValue(request);
        System.out.println("Secret: " + response.secretString());
    }
}
