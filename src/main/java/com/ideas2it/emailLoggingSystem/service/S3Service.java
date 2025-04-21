package com.ideas2it.emailLoggingSystem.service;

import java.io.File;

public interface S3Service {
    String uploadFile(File file);
}