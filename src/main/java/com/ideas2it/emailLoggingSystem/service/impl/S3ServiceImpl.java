package com.ideas2it.emailLoggingSystem.service.impl;

import com.amazonaws.services.s3.model.PutObjectResult;
import com.ideas2it.emailLoggingSystem.service.AWSSecretsManagerService;
import com.ideas2it.emailLoggingSystem.service.S3Service;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

@Service
public class S3ServiceImpl implements S3Service {
    private static final Logger logger = LoggerFactory.getLogger(S3ServiceImpl.class);

    @Value("${aws.bucketName}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    private AmazonS3 s3Client;

    private final AWSSecretsManagerService awsSecretsManagerService;

    @Autowired
    public S3ServiceImpl(AWSSecretsManagerService awsSecretsManagerService) {
        this.awsSecretsManagerService = awsSecretsManagerService;
    }

    /**
     * Initializes the Amazon S3 client using credentials retrieved from AWS Secrets Manager.
     *
     * This method uses the {@link AWSSecretsManagerService} to fetch the AWS credentials (access key and secret key)
     * from the configured Secrets Manager. It then sets up the Amazon S3 client with these credentials,
     * using the specified AWS region.
     *
     * @throws RuntimeException if there is an error initializing the S3 client or fetching the credentials
     */
    @PostConstruct
    public void initS3Client() {
        try {
            logger.info("Initializing S3 client...");

            BasicAWSCredentials awsCredentials = awsSecretsManagerService.getAWSCredentialsFromSecret();
            logger.info("Got AWS credentials successfully.");

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

    /**
     * Uploads a file to the specified Amazon S3 bucket.
     *
     * This method uploads the given {@link File} to the S3 bucket configured in the application properties
     * under the bucket name. It returns the URL of the uploaded file on success.
     *
     * @param file the {@link File} object to be uploaded
     * @return a {@link String} representing the URL of the uploaded file on S3
     * @throws RuntimeException if there is an error during the upload process
     */
    @Override
    public String uploadFile(File file) {
        try {
            String fileName = file.getName();
            PutObjectResult s3URLObject = s3Client.putObject(new PutObjectRequest(bucketName, fileName, file));

            logger.info("S3 Upload Result - ETag: {}, VersionId: {}",
                    s3URLObject.getETag(), s3URLObject.getMetadata().getUserMetadata().values(),
                    s3URLObject.getVersionId() != null ? s3URLObject.getVersionId() : "N/A"
            );
            URL s3Url = s3Client.getUrl(bucketName, fileName);
            return String.valueOf(s3Url);
        } catch (Exception e) {
            logger.error("Failed to upload file from File object: {}", e);
            throw new RuntimeException("File upload failed", e); // You can also create a custom exception for better error handling
        }

    }
}
