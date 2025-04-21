package com.ideas2it.emailLoggingSystem.service;

import com.amazonaws.auth.BasicAWSCredentials;

public interface AWSSecretsManagerService {
    public BasicAWSCredentials getAWSCredentialsFromSecret();
}

