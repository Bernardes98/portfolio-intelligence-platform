package com.portfoliointelligence.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.UUID;

public interface FileStorageService {

    StoredFile store(
            UUID analysisId,
            UUID documentId,
            MultipartFile file
    );

    Path resolve(String storagePath);

    void delete(String storagePath);

    record StoredFile(
            String filename,
            String path
    ) {
    }
}