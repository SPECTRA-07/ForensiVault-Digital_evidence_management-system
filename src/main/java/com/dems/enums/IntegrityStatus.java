package com.dems.enums;

/**
 * Defines integrity verification states for digital evidence files in DEMS.
 */
public enum IntegrityStatus {
    VERIFIED,
    TAMPERED,
    HASH_MISSING,
    FILE_MISSING,
    VERIFICATION_FAILED
}
