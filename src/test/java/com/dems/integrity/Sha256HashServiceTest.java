package com.dems.integrity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class Sha256HashServiceTest {

    private Sha256HashService hashService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        hashService = new Sha256HashService();
    }

    @Test
    void generateHash_FromInputStream_ReturnsKnownSha256() {
        // SHA-256 of "hello world" is "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9"
        ByteArrayInputStream is = new ByteArrayInputStream("hello world".getBytes());

        String hash = hashService.generateHash(is);

        assertNotNull(hash);
        assertEquals("b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9", hash);
    }

    @Test
    void generateHash_FromPath_ReturnsKnownSha256() throws IOException {
        Path filePath = tempDir.resolve("testfile.txt");
        Files.writeString(filePath, "hello world");

        String hash = hashService.generateHash(filePath);

        assertNotNull(hash);
        assertEquals("b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9", hash);
    }
}
