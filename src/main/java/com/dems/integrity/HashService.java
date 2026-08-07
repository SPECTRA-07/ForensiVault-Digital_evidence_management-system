package com.dems.integrity;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * HashService interface defining cryptographic hash generation contracts for evidence integrity verification.
 */
public interface HashService {

    /**
     * Generates a lowercase hex hash string from an InputStream.
     *
     * @param inputStream Stream containing payload data.
     * @return Lowercase hexadecimal hash string.
     */
    String generateHash(InputStream inputStream);

    /**
     * Generates a lowercase hex hash string from a file Path.
     *
     * @param filePath Path pointing to the target file.
     * @return Lowercase hexadecimal hash string.
     */
    String generateHash(Path filePath);
}
