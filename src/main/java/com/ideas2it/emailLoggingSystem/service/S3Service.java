package com.ideas2it.emailLoggingSystem.service;

import java.io.File;

public interface S3Service {

    /** Upload fil in s3 from attachment */
    String uploadFile(File file);
}