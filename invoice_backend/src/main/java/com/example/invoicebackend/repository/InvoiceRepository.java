package com.example.invoicebackend.repository;

import com.example.invoicebackend.model.Invoice;
import com.example.invoicebackend.model.Partner;
import com.example.invoicebackend.model.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByPartnerAndDeletedFalse(Partner partner);
    Optional<Invoice> findByPartnerAndInvoiceNumber(Partner partner, String invoiceNumber);
    List<Invoice> findByPartnerAndStatusAndDeletedFalse(Partner partner, InvoiceStatus status);
    List<Invoice> findByPartnerAndDueDateBeforeAndDeletedFalse(Partner partner, LocalDate dueDate);
}
