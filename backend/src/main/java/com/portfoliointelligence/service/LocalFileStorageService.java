package com.portfoliointelligence.service;

import com.portfoliointelligence.config.StorageProperties;
import com.portfoliointelligence.exception.FileStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalFileStorageService
        implements FileStorageService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(LocalFileStorageService.class);

    private final Path storageRoot;

    public LocalFileStorageService(
            StorageProperties storageProperties
    ) {
        this.storageRoot = storageProperties
                .location()
                .toAbsolutePath()
                .normalize();
    }

    @Override
    public StoredFile store(
            UUID analysisId,
            UUID documentId,
            MultipartFile file
    ) {
        String storageFilename = documentId + ".pdf";

        Path relativePath = Path.of(
                "analyses",
                analysisId.toString(),
                storageFilename
        );

        Path targetPath = storageRoot
                .resolve(relativePath)
                .normalize();

        validateTargetPath(targetPath);

        try {
            Files.createDirectories(targetPath.getParent());

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(
                        inputStream,
                        targetPath,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            return new StoredFile(
                    storageFilename,
                    normalizePath(relativePath)
            );
        } catch (IOException exception) {
            throw new FileStorageException(
                    "Não foi possível armazenar o documento.",
                    exception
            );
        }
    }

    @Override
    public Path resolve(String storagePath) {
        if (!StringUtils.hasText(storagePath)) {
            throw new FileStorageException(
                    "O caminho do documento não foi informado."
            );
        }

        Path targetPath = storageRoot
                .resolve(Path.of(storagePath))
                .normalize();

        validateTargetPath(targetPath);

        if (!Files.isRegularFile(targetPath)) {
            throw new FileStorageException(
                    "O arquivo armazenado não foi encontrado."
            );
        }

        return targetPath;
    }

    @Override
    public void delete(String storagePath) {
        if (!StringUtils.hasText(storagePath)) {
            return;
        }

        Path targetPath = storageRoot
                .resolve(Path.of(storagePath))
                .normalize();

        validateTargetPath(targetPath);

        try {
            Files.deleteIfExists(targetPath);
        } catch (IOException exception) {
            LOGGER.warn(
                    "Não foi possível excluir o arquivo: {}",
                    targetPath,
                    exception
            );
        }
    }

    private void validateTargetPath(Path targetPath) {
        if (!targetPath.startsWith(storageRoot)) {
            throw new FileStorageException(
                    "Caminho de armazenamento inválido."
            );
        }
    }

    private String normalizePath(Path path) {
        return path
                .toString()
                .replace('\\', '/');
    }
}