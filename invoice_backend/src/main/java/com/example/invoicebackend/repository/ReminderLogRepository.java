package com.example.invoicebackend.repository;

import com.example.invoicebackend.model.Invoice;
import com.example.invoicebackend.model.ReminderLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReminderLogRepository extends JpaRepository<ReminderLog, Long> {
    List<ReminderLog> findByInvoice(Invoice invoice);
}
