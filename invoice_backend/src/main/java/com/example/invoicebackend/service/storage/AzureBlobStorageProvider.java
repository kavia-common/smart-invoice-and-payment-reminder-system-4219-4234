package com.example.invoicebackend.service.storage;

import java.io.InputStream;

/**
 * Placeholder Azure Blob provider. Not implemented in MVP to avoid adding SDK dependencies now.
 */
public class AzureBlobStorageProvider implements StorageProvider {

    public AzureBlobStorageProvider(String connectionString) { }

    @Override
    public String save(String path, InputStream input, Long contentLength, String contentType) {
        throw new StorageProviderException("Azure provider not yet implemented; set STORAGE_PROVIDER=local for development");
    }

    @Override
    public InputStream open(String path) {
        throw new StorageProviderException("Azure provider not yet implemented");
    }

    @Override
    public boolean exists(String path) {
        return false;
    }

    @Override
    public boolean delete(String path) {
        return false;
    }
}
