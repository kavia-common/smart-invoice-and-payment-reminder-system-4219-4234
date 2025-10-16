package com.example.invoicebackend.config;

import com.example.invoicebackend.service.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

/**
 * Configures the StorageProvider bean based on environment-driven properties.
 */
@Configuration
public class StorageConfig {

    @Value("${app.storage.provider:local}")
    private String provider;

    @Value("${app.storage.local.path:attachments}")
    private String localPath;

    @Value("${app.storage.s3.bucket:}")
    private String s3Bucket;
    @Value("${app.storage.s3.region:}")
    private String s3Region;
    @Value("${app.storage.s3.accessKey:}")
    private String s3AccessKey;
    @Value("${app.storage.s3.secretKey:}")
    private String s3SecretKey;

    @Value("${app.storage.azure.connection:}")
    private String azureConn;

    // PUBLIC_INTERFACE
    @Bean
    public StorageProvider storageProvider() {
        /** Provide storage provider based on app.storage.provider. Defaults to local. */
        String p = provider == null ? "local" : provider.trim().toLowerCase();
        return switch (p) {
            case "s3" -> new S3StorageProvider(s3Bucket, s3Region, s3AccessKey, s3SecretKey);
            case "azure" -> new AzureBlobStorageProvider(azureConn);
            default -> new LocalStorageProvider(Path.of(localPath));
        };
    }
}
