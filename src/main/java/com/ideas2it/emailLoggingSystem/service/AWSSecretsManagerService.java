package com.ideas2it.emailLoggingSystem.service;

import com.amazonaws.auth.BasicAWSCredentials;

public interface AWSSecretsManagerService {
    /**
     * Retrieves AWS credentials (access key and secret key) from AWS Secrets Manager.
     *
     * @return AWS credentials for further use
     */
    public BasicAWSCredentials getAWSCredentialsFromSecret();
}

