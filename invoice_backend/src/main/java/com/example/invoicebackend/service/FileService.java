package com.example.invoicebackend.service;

import com.example.invoicebackend.model.FileAttachment;
import com.example.invoicebackend.model.Invoice;
import com.example.invoicebackend.model.Partner;
import com.example.invoicebackend.repository.FileAttachmentRepository;
import com.example.invoicebackend.repository.InvoiceRepository;
import com.example.invoicebackend.repository.PartnerRepository;
import com.example.invoicebackend.service.storage.StorageProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.util.Optional;

/**
 * Handles file uploads/downloads via pluggable storage provider and persists metadata.
 */
@Service
public class FileService {

    private final StorageProvider storage;
    private final FileAttachmentRepository attachments;
    private final PartnerRepository partners;
    private final InvoiceRepository invoices;

    public FileService(StorageProvider storage,
                       FileAttachmentRepository attachments,
                       PartnerRepository partners,
                       InvoiceRepository invoices) {
        this.storage = storage;
        this.attachments = attachments;
        this.partners = partners;
        this.invoices = invoices;
    }

    // PUBLIC_INTERFACE
    @Transactional
    public FileAttachment upload(Long partnerId, Long invoiceId, MultipartFile file) {
        /** Save file to storage and persist FileAttachment. */
        Partner partner = partners.findById(partnerId).orElseThrow(() -> new IllegalArgumentException("Partner not found"));
        Invoice invoice = null;
        if (invoiceId != null) {
            invoice = invoices.findById(invoiceId).orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        }
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.bin";
        String logical = buildPath(partner.getId(), invoice != null ? invoice.getId() : null, original);

        try (InputStream in = file.getInputStream()) {
            String storedRef = storage.save(logical, in, file.getSize(), file.getContentType());
            FileAttachment fa = new FileAttachment();
            fa.setPartner(partner);
            fa.setInvoice(invoice);
            fa.setFileName(original);
            fa.setFileUrl(logical); // store logical path; we use provider to open
            fa.setMimeType(file.getContentType());
            fa.setSizeBytes(file.getSize());
            fa.setCreatedAt(Instant.now());
            fa.setUpdatedAt(Instant.now());
            return attachments.save(fa);
        } catch (Exception e) {
            throw new IllegalArgumentException("Upload failed: " + e.getMessage());
        }
    }

    private String buildPath(Long partnerId, Long invoiceId, String name) {
        String safe = name.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (invoiceId != null) {
            return "partners/" + partnerId + "/invoices/" + invoiceId + "/" + safe;
        }
        return "partners/" + partnerId + "/uploads/" + safe;
    }

    // PUBLIC_INTERFACE
    @Transactional(readOnly = true)
    public Optional<FileAttachment> findAttachment(Long id) {
        /** Find attachment metadata by id. */
        return attachments.findById(id);
    }

    // PUBLIC_INTERFACE
    @Transactional(readOnly = true)
    public InputStream openStream(FileAttachment fa) {
        /** Open stream using provider and stored logical path. */
        return storage.open(fa.getFileUrl());
    }
}
