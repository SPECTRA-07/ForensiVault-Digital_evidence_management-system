package com.dems.integrity;

import com.dems.exception.InternalServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Service generating SHA-256 cryptographic digests over file input streams using 8KB buffers for memory safety.
 */
@Slf4j
@Service
public class Sha256HashService implements HashService {

    private static final String ALGORITHM = "SHA-256";
    private static final int BUFFER_SIZE = 8192; // 8 KB

    @Override
    public String generateHash(InputStream inputStream) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hashBytes = digest.digest();
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm unavailable", e);
            throw new InternalServerException("Cryptographic algorithm SHA-256 unavailable.", e);
        } catch (IOException e) {
            log.error("Failed to read stream for SHA-256 digest computation", e);
            throw new InternalServerException("Failed to read stream content for hash generation.", e);
        }
    }

    @Override
    public String generateHash(Path filePath) {
        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            log.warn("Cannot generate hash: File path [{}] does not exist or is unreadable", filePath);
            throw new InternalServerException("Target file for hash generation does not exist or is unreadable: " + filePath);
        }

        try (InputStream is = Files.newInputStream(filePath)) {
            return generateHash(is);
        } catch (IOException e) {
            log.error("Error opening InputStream for file [{}]", filePath, e);
            throw new InternalServerException("Failed to open file for cryptographic hashing.", e);
        }
    }
}
