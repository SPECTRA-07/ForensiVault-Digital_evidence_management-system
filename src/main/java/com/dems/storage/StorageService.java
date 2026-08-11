package com.dems.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * StorageService interface defining pluggable file storage contracts for DEMS.
 * Supports both local filesystem storage and persistent S3-compatible cloud object storage.
 */
public interface StorageService {

    /**
     * Stores an incoming multipart evidence file under the specified case container.
     *
     * @param file       The multipart file payload.
     * @param caseNumber The unique case identifier for partitioning.
     * @return The stored path or object key.
     */
    String storeFile(MultipartFile file, String caseNumber);

    /**
     * Stores raw byte content (e.g. generated QR code image) under a target subdirectory.
     *
     * @param content  The file payload bytes.
     * @param fileName The target filename.
     * @param subDir   The target subdirectory or prefix (e.g. "qr").
     * @return The stored path or object key.
     */
    String storeFile(byte[] content, String fileName, String subDir);

    /**
     * Loads a stored file resource given its storage path or object key.
     *
     * @param storagePath The relative storage path or object key.
     * @return The file as a Spring Resource.
     */
    Resource loadFileAsResource(String storagePath);

    /**
     * Loads a stored file input stream directly for streaming computations (e.g. SHA-256 integrity check).
     *
     * @param storagePath The relative storage path or object key.
     * @return An InputStream of the stored object.
     * @throws IOException If an I/O error occurs.
     */
    default InputStream loadFileAsStream(String storagePath) throws IOException {
        return loadFileAsResource(storagePath).getInputStream();
    }

    /**
     * Deletes a file from storage if present.
     *
     * @param storagePath The storage path or object key to delete.
     */
    void deleteFile(String storagePath);
}
