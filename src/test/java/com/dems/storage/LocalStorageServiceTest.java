package com.dems.storage;

import com.dems.config.StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalStorageServiceImpl storageService;

    @BeforeEach
    void setUp() {
        StorageProperties properties = new StorageProperties();
        properties.setLocation(tempDir.toString());
        storageService = new LocalStorageServiceImpl(properties);
    }

    @Test
    void storeFile_And_LoadAsResource_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "sample_evidence.png", "image/png", "sample content".getBytes());

        String storagePath = storageService.storeFile(file, "CASE-2026-001");

        assertNotNull(storagePath);
        assertTrue(storagePath.contains("CASE-2026-001"));

        Resource resource = storageService.loadFileAsResource(storagePath);
        assertNotNull(resource);
        assertTrue(resource.exists());
    }
}
