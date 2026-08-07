package com.dems.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * StorageService interface defining pluggable file storage contracts for DEMS.
 * Allows seamless extension from local filesystem to cloud storage (e.g. AWS S3) in future releases.
 */
public interface StorageService {

    /**
     * Stores an incoming multipart file in storage under the given case container.
     *
     * @param file       The multipart file payload.
     * @param caseNumber The unique case identifier for directory structuring.
     * @return The stored relative or absolute path of the file.
     */
    String storeFile(MultipartFile file, String caseNumber);

    /**
     * Loads a file resource from storage given its storage path.
     *
     * @param storagePath The relative or absolute path of the file.
     * @return The file as a Spring Resource.
     */
    Resource loadFileAsResource(String storagePath);

    /**
     * Deletes a file from storage if present.
     *
     * @param storagePath The storage path to delete.
     */
    void deleteFile(String storagePath);
}
