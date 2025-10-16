package com.example.invoicebackend.web;

import com.example.invoicebackend.model.FileAttachment;
import com.example.invoicebackend.service.FileService;
import com.example.invoicebackend.web.dto.FileDtos.FileUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * REST endpoints for file upload and download.
 */
@RestController
@RequestMapping("/api/files")
@Validated
@Tag(name = "Files", description = "File upload/download endpoints")
public class FilesController {

    private final FileService files;

    public FilesController(FileService files) {
        this.files = files;
    }

    // PUBLIC_INTERFACE
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    @Operation(summary = "Upload a file", description = "Upload a file. Optionally link to an invoice using invoiceId.")
    public ResponseEntity<FileUploadResponse> upload(
            @RequestParam @NotNull Long partnerId,
            @RequestParam(required = false) Long invoiceId,
            @RequestParam("file") MultipartFile file
    ) {
        var fa = files.upload(partnerId, invoiceId, file);
        FileUploadResponse resp = new FileUploadResponse();
        resp.id = fa.getId();
        resp.fileName = fa.getFileName();
        resp.mimeType = fa.getMimeType();
        resp.sizeBytes = fa.getSizeBytes();
        resp.partnerId = fa.getPartner().getId();
        resp.invoiceId = fa.getInvoice() != null ? fa.getInvoice().getId() : null;
        return ResponseEntity.ok(resp);
    }

    // PUBLIC_INTERFACE
    @GetMapping("/{id}/download")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    @Operation(summary = "Download a file", description = "Download file content by attachment id.")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long id) {
        FileAttachment fa = files.findAttachment(id).orElseThrow(() -> new IllegalArgumentException("File not found"));
        var stream = files.openStream(fa);
        String contentType = fa.getMimeType() != null ? fa.getMimeType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        String encoded = URLEncoder.encode(fa.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(fa.getSizeBytes() != null ? fa.getSizeBytes() : -1)
                .body(new InputStreamResource(stream));
    }
}
