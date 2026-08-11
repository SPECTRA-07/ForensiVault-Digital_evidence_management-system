package com.dems.storage;

import com.dems.config.StorageProperties;
import com.dems.exception.InternalServerException;
import com.dems.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Objects;
import java.util.UUID;

/**
 * Persistent S3-compatible cloud object storage implementation of StorageService.
 * Supports AWS S3, Cloudflare R2, MinIO, and DigitalOcean Spaces.
 * Active when app.storage.provider=s3.
 * Securely uses AWS SDK DefaultCredentialsProvider (environment variable / IAM role resolution).
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "s3")
public class S3StorageServiceImpl implements StorageService {

    private final S3Client s3Client;
    private final String bucket;

    @org.springframework.beans.factory.annotation.Autowired
    public S3StorageServiceImpl(StorageProperties storageProperties) {
        this(storageProperties, createS3Client(storageProperties));
    }

    public S3StorageServiceImpl(StorageProperties storageProperties, S3Client s3Client) {
        this.s3Client = s3Client;
        this.bucket = storageProperties.getBucket();
        log.info("Initialized S3StorageServiceImpl for bucket [{}] and region [{}]",
                this.bucket, storageProperties.getRegion());
    }

    private static S3Client createS3Client(StorageProperties properties) {
        S3ClientBuilder builder = S3Client.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .serviceConfiguration(S3Configuration.builder()
                        .chunkedEncodingEnabled(false)
                        .pathStyleAccessEnabled(true)
                        .build());

        if (StringUtils.hasText(properties.getRegion())) {
            builder.region(Region.of(properties.getRegion().trim()));
        } else {
            builder.region(Region.US_EAST_1);
        }

        if (StringUtils.hasText(properties.getEndpoint())) {
            builder.endpointOverride(URI.create(properties.getEndpoint().trim()));
        }

        return builder.build();
    }

    @Override
    public String storeFile(MultipartFile file, String caseNumber) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Failed to store empty file.");
        }

        String rawOriginalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = getFileExtension(rawOriginalFilename);
        String storedFileName = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);

        // Object Key Structure: cases/{caseNumber}/{storedFileName}
        String objectKey = "cases/" + caseNumber + "/" + storedFileName;

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(this.bucket)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
            log.info("Successfully uploaded object [{}] to S3 bucket [{}]", objectKey, this.bucket);
            return objectKey;

        } catch (S3Exception e) {
            log.error("S3 error uploading file [{}] to bucket [{}]", objectKey, this.bucket, e);
            throw new InternalServerException("Failed to store file in S3 cloud storage: " + e.awsErrorDetails().errorMessage(), e);
        } catch (IOException e) {
            log.error("I/O error uploading file [{}] to S3 bucket [{}]", objectKey, this.bucket, e);
            throw new InternalServerException("Failed to read file payload for S3 upload.", e);
        }
    }

    @Override
    public String storeFile(byte[] content, String fileName, String subDir) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("Cannot store empty content byte array.");
        }

        // Object Key Structure: {subDir}/{fileName}
        String objectKey = (StringUtils.hasText(subDir) ? subDir.trim() + "/" : "") + fileName;

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(this.bucket)
                    .key(objectKey)
                    .contentLength((long) content.length)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(content));
            log.info("Successfully uploaded byte content object [{}] to S3 bucket [{}]", objectKey, this.bucket);
            return objectKey;

        } catch (S3Exception e) {
            log.error("S3 error uploading byte content [{}] to bucket [{}]", objectKey, this.bucket, e);
            throw new InternalServerException("Failed to store byte content in S3 cloud storage: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    @Override
    public Resource loadFileAsResource(String storagePath) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(this.bucket)
                    .key(storagePath)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Stream = s3Client.getObject(getRequest);
            GetObjectResponse response = s3Stream.response();

            String filename = storagePath.contains("/") ? storagePath.substring(storagePath.lastIndexOf('/') + 1) : storagePath;

            return new NamedS3Resource(s3Stream, filename, response.contentLength());

        } catch (NoSuchKeyException e) {
            log.warn("S3 object key [{}] not found in bucket [{}]", storagePath, this.bucket);
            throw new ResourceNotFoundException("Evidence file not found in S3 storage key: " + storagePath);
        } catch (S3Exception e) {
            log.error("Error retrieving S3 object [{}] from bucket [{}]", storagePath, this.bucket, e);
            throw new InternalServerException("Failed to retrieve evidence file from S3 cloud storage.", e);
        }
    }

    @Override
    public void deleteFile(String storagePath) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(this.bucket)
                    .key(storagePath)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Deleted object [{}] from S3 bucket [{}]", storagePath, this.bucket);
        } catch (S3Exception e) {
            log.warn("Failed to delete object [{}] from S3 bucket [{}]", storagePath, this.bucket, e);
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1).toLowerCase();
    }

    /**
     * Custom InputStreamResource retaining filename and content length metadata for HTTP streaming response headers.
     */
    private static class NamedS3Resource extends InputStreamResource {

        private final String filename;
        private final long contentLength;

        public NamedS3Resource(InputStream inputStream, String filename, long contentLength) {
            super(inputStream);
            this.filename = filename;
            this.contentLength = contentLength;
        }

        @Override
        public String getFilename() {
            return this.filename;
        }

        @Override
        public long contentLength() {
            return this.contentLength;
        }
    }
}
