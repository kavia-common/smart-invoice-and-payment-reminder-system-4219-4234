package com.example.invoicebackend.service.storage;

/**
 * Exception type for storage provider errors.
 */
public class StorageProviderException extends RuntimeException {
    public StorageProviderException(String message) { super(message); }
    public StorageProviderException(String message, Throwable cause) { super(message, cause); }
}
