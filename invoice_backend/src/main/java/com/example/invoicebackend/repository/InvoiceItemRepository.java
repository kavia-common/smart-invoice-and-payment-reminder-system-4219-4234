package com.example.invoicebackend.repository;

import com.example.invoicebackend.model.Invoice;
import com.example.invoicebackend.model.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {
    List<InvoiceItem> findByInvoice(Invoice invoice);
}
