package code.springboot.poject.emailLoggingSystem.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    @Value("${aws.bucketName}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    private AmazonS3 s3Client;

    private final AWSSecretsManagerService awsSecretsManagerService;

    @Autowired
    public S3Service(AWSSecretsManagerService awsSecretsManagerService) {
        this.awsSecretsManagerService = awsSecretsManagerService;
    }

    @PostConstruct
    public void initS3Client() {
        try {
            System.out.println("Initializing S3 client...");
            BasicAWSCredentials awsCredentials = awsSecretsManagerService.getAWSCredentialsFromSecret();
            System.out.println("Got AWS credentials successfully.");

            this.s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withRegion(region)
                    .build();

            logger.info("S3 client initialized successfully.");
        } catch (Exception e) {
            e.printStackTrace(); // Add this to print the full root cause
            throw new RuntimeException("Failed to initialize S3 client", e);
        }
    }

    public String uploadFile(File file) {
        String fileName = file.getName();
        try {
            // Upload the file to S3
            s3Client.putObject(new PutObjectRequest(bucketName, fileName, file));
            logger.info("File uploaded successfully: {}", fileName);

            // Return the URL to the uploaded file
            return s3Client.getUrl(bucketName, fileName).toString();
        } catch (Exception e) {
            logger.error("Failed to upload file: {}", fileName, e);
            throw new RuntimeException("File upload failed", e); // You can create a custom exception for better handling
        }
    }
}
