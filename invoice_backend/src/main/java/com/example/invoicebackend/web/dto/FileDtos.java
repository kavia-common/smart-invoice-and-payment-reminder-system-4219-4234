package com.example.invoicebackend.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTOs for file APIs.
 */
public class FileDtos {

    public static class FileUploadResponse {
        @Schema(description = "Attachment id")
        public Long id;
        public String fileName;
        public String mimeType;
        public Long sizeBytes;
        public Long invoiceId;
        public Long partnerId;
    }
}
