package com.example.invoicebackend.repository;

import com.example.invoicebackend.model.Invoice;
import com.example.invoicebackend.model.Payment;
import com.example.invoicebackend.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByInvoice(Invoice invoice);
    List<Payment> findByInvoiceAndStatus(Invoice invoice, PaymentStatus status);
}
