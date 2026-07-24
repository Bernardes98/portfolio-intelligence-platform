package com.portfoliointelligence.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileStorageService {

    StoredFile store(
            UUID analysisId,
            UUID documentId,
            MultipartFile file
    );

    void delete(String storagePath);

    record StoredFile(
            String filename,
            String path
    ) {
    }
}