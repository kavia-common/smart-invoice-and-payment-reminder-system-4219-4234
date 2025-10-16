package com.example.invoicebackend.service.storage;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Local filesystem-backed storage. Base directory provided via configuration.
 */
public class LocalStorageProvider implements StorageProvider {

    private final Path baseDir;

    public LocalStorageProvider(Path baseDir) {
        this.baseDir = baseDir;
    }

    private Path resolvePath(String path) {
        // Prevent path traversal
        Path p = baseDir.resolve(path).normalize();
        if (!p.startsWith(baseDir)) {
            throw new StorageProviderException("Invalid storage path");
        }
        return p;
    }

    @Override
    public String save(String path, InputStream input, Long contentLength, String contentType) {
        try {
            Path target = resolvePath(path);
            Files.createDirectories(target.getParent());
            try (OutputStream out = Files.newOutputStream(target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                IOUtils.copy(input, out);
            }
            // Return a relative URL/path usable by download endpoint
            return target.toString();
        } catch (IOException e) {
            throw new StorageProviderException("Failed to save file locally", e);
        }
    }

    @Override
    public InputStream open(String path) {
        try {
            Path p = resolvePath(path);
            return Files.newInputStream(p, StandardOpenOption.READ);
        } catch (IOException e) {
            throw new StorageProviderException("Failed to open file", e);
        }
    }

    @Override
    public boolean exists(String path) {
        return Files.exists(resolvePath(path));
    }

    @Override
    public boolean delete(String path) {
        try {
            Path p = resolvePath(path);
            if (Files.exists(p)) {
                Files.delete(p);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
