package com.example.invoicebackend.repository;

import com.example.invoicebackend.model.FileAttachment;
import com.example.invoicebackend.model.Invoice;
import com.example.invoicebackend.model.Partner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {
    List<FileAttachment> findByInvoice(Invoice invoice);
    List<FileAttachment> findByPartner(Partner partner);
}
