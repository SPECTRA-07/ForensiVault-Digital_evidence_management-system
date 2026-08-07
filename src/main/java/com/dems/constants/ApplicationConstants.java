package com.dems.constants;

/**
 * System-wide application constants for DEMS foundation.
 */
public final class ApplicationConstants {

    public static final String APPLICATION_NAME = "Digital Evidence Management System";
    public static final String API_VERSION = "v1";
    public static final String DEFAULT_UPLOAD_DIRECTORY = "uploads";

    private ApplicationConstants() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Utility constant class cannot be instantiated.");
    }
}
