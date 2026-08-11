package com.dems.storage;

import com.dems.config.StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3StorageServiceImplTest {

    @Mock
    private S3Client s3Client;

    private StorageProperties storageProperties;
    private S3StorageServiceImpl s3StorageService;

    @BeforeEach
    void setUp() {
        storageProperties = new StorageProperties();
        storageProperties.setProvider("s3");
        storageProperties.setBucket("test-bucket");
        storageProperties.setRegion("us-east-1");

        s3StorageService = new S3StorageServiceImpl(storageProperties, s3Client);
    }

    @Test
    void storeFile_MultipartFile_Success() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "evidence.pdf", "application/pdf", "Test PDF Content".getBytes()
        );

        String objectKey = s3StorageService.storeFile(file, "CASE-2026-001");

        assertNotNull(objectKey);
        assertEquals(true, objectKey.startsWith("cases/CASE-2026-001/"));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void storeFile_ByteArray_Success() {
        byte[] qrBytes = "PNG Bytes".getBytes();

        String objectKey = s3StorageService.storeFile(qrBytes, "QR-EVD-100.png", "qr");

        assertNotNull(objectKey);
        assertEquals("qr/QR-EVD-100.png", objectKey);
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void loadFileAsResource_Success() {
        GetObjectResponse getResponse = GetObjectResponse.builder()
                .contentLength(16L)
                .contentType("application/pdf")
                .build();
        InputStream stream = new ByteArrayInputStream("Test PDF Content".getBytes());
        ResponseInputStream<GetObjectResponse> responseInputStream = new ResponseInputStream<>(getResponse, stream);

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseInputStream);

        Resource resource = s3StorageService.loadFileAsResource("cases/CASE-2026-001/file.pdf");

        assertNotNull(resource);
        assertEquals("file.pdf", resource.getFilename());
    }

    @Test
    void deleteFile_Success() {
        s3StorageService.deleteFile("cases/CASE-2026-001/file.pdf");

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }
}
