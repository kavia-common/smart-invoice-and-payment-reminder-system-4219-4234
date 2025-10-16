package com.example.invoicebackend.service.storage;

import java.io.InputStream;

/**
 * Placeholder S3 provider interface-compatible implementation.
 * Note: For MVP we do not hard-require AWS SDK dependencies; this class throws if used.
 * Future enhancement: implement with AWS SDK v2 S3Client using env credentials.
 */
public class S3StorageProvider implements StorageProvider {

    public S3StorageProvider(String bucket, String region, String accessKey, String secretKey) {
        // Store config; not used in MVP placeholder
    }

    @Override
    public String save(String path, InputStream input, Long contentLength, String contentType) {
        throw new StorageProviderException("S3 provider not yet implemented; set STORAGE_PROVIDER=local for development");
    }

    @Override
    public InputStream open(String path) {
        throw new StorageProviderException("S3 provider not yet implemented");
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
