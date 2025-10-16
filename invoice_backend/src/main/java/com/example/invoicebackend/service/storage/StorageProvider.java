package com.example.invoicebackend.service.storage;

import java.io.InputStream;

/**
 * PUBLIC_INTERFACE
 * Abstraction for file storage providers. Implementations may store files locally or in cloud.
 */
public interface StorageProvider {

    /**
     * PUBLIC_INTERFACE
     * Save a file stream to storage under a logical path and return a resolvable URL or path.
     *
     * @param path logical path (e.g., "invoices/123/filename.pdf")
     * @param input content stream (will be consumed; caller closes)
     * @param contentLength optional length; may be null if unknown
     * @param contentType mime type hint
     * @return a string representing a URL or path to the stored object
     * @throws StorageProviderException when saving fails
     */
    String save(String path, InputStream input, Long contentLength, String contentType) throws StorageProviderException;

    /**
     * PUBLIC_INTERFACE
     * Open an InputStream for a stored object.
     *
     * @param path logical path used when saving
     * @return InputStream for reading
     * @throws StorageProviderException when retrieval fails
     */
    InputStream open(String path) throws StorageProviderException;

    /**
     * PUBLIC_INTERFACE
     * @param path logical path
     * @return true if exists
     */
    boolean exists(String path);

    /**
     * PUBLIC_INTERFACE
     * Delete object if supported; returns true if deleted or absent.
     *
     * @param path logical path
     * @return success boolean
     */
    boolean delete(String path);
}
