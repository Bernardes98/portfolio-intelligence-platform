package com.portfoliointelligence.service;

import com.portfoliointelligence.entity.FinancialInstitution;
import com.portfoliointelligence.exception.DocumentProcessingException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class PdfTextExtractionService {

    private final FileStorageService fileStorageService;
    private final FinancialInstitutionDetector institutionDetector;

    public PdfTextExtractionService(
            FileStorageService fileStorageService,
            FinancialInstitutionDetector institutionDetector
    ) {
        this.fileStorageService = fileStorageService;
        this.institutionDetector = institutionDetector;
    }

    public PdfExtractionResult extract(String storagePath) {
        Path pdfPath = fileStorageService.resolve(storagePath);

        try (PDDocument document =
                     Loader.loadPDF(pdfPath.toFile())) {

            int pageCount = document.getNumberOfPages();

            if (pageCount <= 0) {
                throw new DocumentProcessingException(
                        "O PDF não possui páginas."
                );
            }

            PDFTextStripper textStripper =
                    new PDFTextStripper();

            String extractedText = normalizeText(
                    textStripper.getText(document)
            );

            if (extractedText.isBlank()) {
                throw new DocumentProcessingException(
                        "O PDF não possui texto extraível. "
                                + "Documentos digitalizados precisarão "
                                + "de processamento por OCR."
                );
            }

            FinancialInstitution institution =
                    institutionDetector.detect(extractedText);

            return new PdfExtractionResult(
                    institution,
                    pageCount,
                    extractedText
            );
        } catch (InvalidPasswordException exception) {
            throw new DocumentProcessingException(
                    "O PDF está protegido por senha.",
                    exception
            );
        } catch (IOException exception) {
            throw new DocumentProcessingException(
                    "Não foi possível ler o conteúdo do PDF.",
                    exception
            );
        }
    }

    private String normalizeText(String value) {
        return value
                .replace("\u0000", "")
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replaceAll("[\\t\\x0B\\f]+", " ")
                .replaceAll(" {2,}", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    public record PdfExtractionResult(
            FinancialInstitution institution,
            int pageCount,
            String extractedText
    ) {
    }
}