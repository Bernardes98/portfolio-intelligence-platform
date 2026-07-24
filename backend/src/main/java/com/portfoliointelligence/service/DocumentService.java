package com.portfoliointelligence.service;

import com.portfoliointelligence.config.StorageProperties;
import com.portfoliointelligence.dto.DocumentResponse;
import com.portfoliointelligence.entity.InvestmentDocument;
import com.portfoliointelligence.entity.PortfolioAnalysis;
import com.portfoliointelligence.exception.DocumentLimitExceededException;
import com.portfoliointelligence.exception.DuplicateDocumentException;
import com.portfoliointelligence.exception.InvalidAnalysisStatusException;
import com.portfoliointelligence.exception.InvalidDocumentException;
import com.portfoliointelligence.repository.InvestmentDocumentRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class DocumentService {

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of(
                    MediaType.APPLICATION_PDF_VALUE,
                    "application/x-pdf",
                    MediaType.APPLICATION_OCTET_STREAM_VALUE
            );

    private final InvestmentDocumentRepository documentRepository;
    private final AnalysisService analysisService;
    private final FileStorageService fileStorageService;
    private final StorageProperties storageProperties;

    public DocumentService(
            InvestmentDocumentRepository documentRepository,
            AnalysisService analysisService,
            FileStorageService fileStorageService,
            StorageProperties storageProperties
    ) {
        this.documentRepository = documentRepository;
        this.analysisService = analysisService;
        this.fileStorageService = fileStorageService;
        this.storageProperties = storageProperties;
    }

    @Transactional
    public List<DocumentResponse> upload(
            UUID analysisId,
            List<MultipartFile> files
    ) {
        PortfolioAnalysis analysis =
                analysisService.getEntity(analysisId);

        validateAnalysisStatus(analysis);
        validateUploadQuantity(analysisId, files);

        List<FileCandidate> candidates = files
                .stream()
                .map(this::validateAndPrepare)
                .toList();

        validateDuplicates(analysisId, candidates);

        List<String> storedPaths = new ArrayList<>();
        registerRollbackCleanup(storedPaths);

        List<InvestmentDocument> documents =
                new ArrayList<>();

        for (FileCandidate candidate : candidates) {
            UUID documentId = UUID.randomUUID();

            FileStorageService.StoredFile storedFile =
                    fileStorageService.store(
                            analysisId,
                            documentId,
                            candidate.file()
                    );

            storedPaths.add(storedFile.path());

            InvestmentDocument document =
                    new InvestmentDocument(
                            documentId,
                            analysisId,
                            candidate.originalFilename(),
                            storedFile.filename(),
                            storedFile.path(),
                            MediaType.APPLICATION_PDF_VALUE,
                            candidate.file().getSize(),
                            candidate.checksum()
                    );

            documents.add(document);
        }

        List<InvestmentDocument> savedDocuments =
                documentRepository.saveAll(documents);

        analysis.registerUploadedDocuments(
                savedDocuments.size(),
                storageProperties.maxFilesPerAnalysis()
        );

        return savedDocuments
                .stream()
                .map(DocumentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> findByAnalysis(
            UUID analysisId
    ) {
        analysisService.getEntity(analysisId);

        return documentRepository
                .findAllByAnalysisIdOrderByCreatedAtAsc(
                        analysisId
                )
                .stream()
                .map(DocumentResponse::from)
                .toList();
    }

    private void validateAnalysisStatus(
            PortfolioAnalysis analysis
    ) {
        if (!analysis.acceptsDocumentUpload()) {
            throw new InvalidAnalysisStatusException(
                    "Documents cannot be uploaded when the analysis "
                            + "status is "
                            + analysis.getStatus()
                            + "."
            );
        }
    }

    private void validateUploadQuantity(
            UUID analysisId,
            List<MultipartFile> files
    ) {
        if (files == null || files.isEmpty()) {
            throw new InvalidDocumentException(
                    "At least one PDF document must be provided."
            );
        }

        int maxFiles =
                storageProperties.maxFilesPerAnalysis();

        long existingDocuments =
                documentRepository.countByAnalysisId(
                        analysisId
                );

        if (existingDocuments + files.size() > maxFiles) {
            throw new DocumentLimitExceededException(
                    "An analysis can contain at most "
                            + maxFiles
                            + " documents."
            );
        }
    }

    private FileCandidate validateAndPrepare(
            MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw new InvalidDocumentException(
                    "Empty documents are not allowed."
            );
        }

        if (file.getSize()
                > storageProperties.maxFileSize().toBytes()) {
            throw new InvalidDocumentException(
                    "The document exceeds the maximum size of "
                            + storageProperties.maxFileSize()
                            + "."
            );
        }

        String originalFilename =
                sanitizeFilename(
                        file.getOriginalFilename()
                );

        if (!originalFilename
                .toLowerCase(Locale.ROOT)
                .endsWith(".pdf")) {
            throw new InvalidDocumentException(
                    "Only files with the .pdf extension "
                            + "are allowed."
            );
        }

        validateContentType(file);
        validatePdfSignature(file);

        return new FileCandidate(
                file,
                originalFilename,
                calculateChecksum(file)
        );
    }

    private void validateContentType(
            MultipartFile file
    ) {
        String contentType = file.getContentType();

        if (!StringUtils.hasText(contentType)) {
            return;
        }

        String normalizedContentType =
                contentType.toLowerCase(Locale.ROOT);

        if (!ALLOWED_CONTENT_TYPES.contains(
                normalizedContentType
        )) {
            throw new InvalidDocumentException(
                    "Only PDF documents are allowed."
            );
        }
    }

    private void validatePdfSignature(
            MultipartFile file
    ) {
        try (InputStream inputStream =
                     file.getInputStream()) {

            byte[] firstBytes =
                    inputStream.readNBytes(1024);

            String header = new String(
                    firstBytes,
                    StandardCharsets.ISO_8859_1
            );

            if (!header.contains("%PDF-")) {
                throw new InvalidDocumentException(
                        "The uploaded file is not a valid PDF."
                );
            }
        } catch (IOException exception) {
            throw new InvalidDocumentException(
                    "Could not read the uploaded document."
            );
        }
    }

    private String calculateChecksum(
            MultipartFile file
    ) {
        try {
            MessageDigest messageDigest =
                    MessageDigest.getInstance("SHA-256");

            try (InputStream inputStream =
                         file.getInputStream()) {

                byte[] buffer = new byte[8192];
                int bytesRead;

                while ((bytesRead =
                        inputStream.read(buffer)) != -1) {
                    messageDigest.update(
                            buffer,
                            0,
                            bytesRead
                    );
                }
            }

            return HexFormat
                    .of()
                    .formatHex(
                            messageDigest.digest()
                    );
        } catch (
                IOException |
                NoSuchAlgorithmException exception
        ) {
            throw new InvalidDocumentException(
                    "Could not calculate the document checksum."
            );
        }
    }

    private void validateDuplicates(
            UUID analysisId,
            List<FileCandidate> candidates
    ) {
        Set<String> requestChecksums =
                new HashSet<>();

        for (FileCandidate candidate : candidates) {
            if (!requestChecksums.add(
                    candidate.checksum()
            )) {
                throw new DuplicateDocumentException(
                        "The same document was included more "
                                + "than once in the request."
                );
            }

            boolean alreadyExists =
                    documentRepository
                            .existsByAnalysisIdAndChecksum(
                                    analysisId,
                                    candidate.checksum()
                            );

            if (alreadyExists) {
                throw new DuplicateDocumentException(
                        "This document has already been uploaded "
                                + "to the analysis."
                );
            }
        }
    }

    private String sanitizeFilename(
            String originalFilename
    ) {
        if (!StringUtils.hasText(originalFilename)) {
            throw new InvalidDocumentException(
                    "The document filename is required."
            );
        }

        String normalizedFilename =
                originalFilename.replace('\\', '/');

        int lastSeparator =
                normalizedFilename.lastIndexOf('/');

        String filename = normalizedFilename.substring(
                lastSeparator + 1
        );

        if (!StringUtils.hasText(filename)) {
            throw new InvalidDocumentException(
                    "The document filename is invalid."
            );
        }

        return filename;
    }

    private void registerRollbackCleanup(
            List<String> storedPaths
    ) {
        if (!TransactionSynchronizationManager
                .isSynchronizationActive()) {
            return;
        }

        TransactionSynchronizationManager
                .registerSynchronization(
                        new TransactionSynchronization() {

                            @Override
                            public void afterCompletion(
                                    int status
                            ) {
                                if (status
                                        == STATUS_ROLLED_BACK) {

                                    storedPaths.forEach(
                                            fileStorageService::delete
                                    );
                                }
                            }
                        }
                );
    }

    private record FileCandidate(
            MultipartFile file,
            String originalFilename,
            String checksum
    ) {
    }
}