package com.dems.storage;

import com.dems.config.StorageProperties;
import com.dems.exception.InternalServerException;
import com.dems.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

/**
 * Local filesystem implementation of StorageService using Java NIO.
 * Stores evidence files organized by case container in uploads/cases/{caseNumber}/ and QR images under uploads/qr/.
 * Active when app.storage.provider=local (or missing).
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalStorageServiceImpl implements StorageService {

    private final Path rootLocation;

    public LocalStorageServiceImpl(StorageProperties storageProperties) {
        this.rootLocation = Paths.get(storageProperties.getLocation()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            log.error("Could not initialize root storage directory at [{}]", this.rootLocation, e);
            throw new InternalServerException("Could not initialize file storage directory location.", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String caseNumber) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Failed to store empty file.");
        }

        String rawOriginalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = getFileExtension(rawOriginalFilename);

        // Generate unique stored filename
        String storedFileName = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);

        // Structure: uploads/cases/{caseNumber}/
        Path caseDir = this.rootLocation.resolve("cases").resolve(caseNumber).normalize();

        try {
            Files.createDirectories(caseDir);
            Path destinationFile = caseDir.resolve(storedFileName).normalize();

            // Security check: ensure target destination is inside root location
            if (!destinationFile.getParent().startsWith(this.rootLocation)) {
                log.warn("Path traversal security violation attempted: [{}]", rawOriginalFilename);
                throw new InternalServerException("Cannot store file outside target root directory.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("File successfully stored at [{}] for Case [{}]", destinationFile, caseNumber);
            return destinationFile.toString();

        } catch (IOException e) {
            log.error("Failed to store file [{}] for Case [{}]", rawOriginalFilename, caseNumber, e);
            throw new InternalServerException("Failed to store file in local storage.", e);
        }
    }

    @Override
    public String storeFile(byte[] content, String fileName, String subDir) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("Cannot store empty content byte array.");
        }

        Path targetDir = this.rootLocation.resolve(subDir).normalize();
        try {
            Files.createDirectories(targetDir);
            Path destinationFile = targetDir.resolve(fileName).normalize();

            try (InputStream inputStream = new ByteArrayInputStream(content)) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("Content successfully stored at [{}] under subDir [{}]", destinationFile, subDir);
            return destinationFile.toString();

        } catch (IOException e) {
            log.error("Failed to store content file [{}] under subDir [{}]", fileName, subDir, e);
            throw new InternalServerException("Failed to store byte content in local storage.", e);
        }
    }

    @Override
    public Resource loadFileAsResource(String storagePath) {
        try {
            Path filePath = Paths.get(storagePath).normalize();
            if (!filePath.isAbsolute()) {
                filePath = this.rootLocation.resolve(filePath).normalize();
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                log.warn("Resource not found or unreadable at path [{}]", storagePath);
                throw new ResourceNotFoundException("Evidence file not found at storage path: " + storagePath);
            }
        } catch (MalformedURLException e) {
            log.error("Invalid file path URL [{}]", storagePath, e);
            throw new ResourceNotFoundException("Invalid evidence file path: " + storagePath, e);
        }
    }

    @Override
    public void deleteFile(String storagePath) {
        try {
            Path filePath = Paths.get(storagePath).normalize();
            if (!filePath.isAbsolute()) {
                filePath = this.rootLocation.resolve(filePath).normalize();
            }
            Files.deleteIfExists(filePath);
            log.info("Deleted file at storage path [{}]", storagePath);
        } catch (IOException e) {
            log.warn("Failed to delete file at storage path [{}]", storagePath, e);
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1).toLowerCase();
    }
}
