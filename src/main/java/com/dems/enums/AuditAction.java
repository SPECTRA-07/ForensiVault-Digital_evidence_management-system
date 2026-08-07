package com.dems.enums;

/**
 * Defines action types for immutable system audit logs in DEMS.
 */
public enum AuditAction {
    LOGIN,
    LOGOUT,
    CREATE,
    UPDATE,
    DELETE_ATTEMPT,
    UPLOAD,
    DOWNLOAD,
    VERIFY,
    TRANSFER,
    ASSIGN,
    STATUS_CHANGE,
    SEARCH,
    VIEW
}
