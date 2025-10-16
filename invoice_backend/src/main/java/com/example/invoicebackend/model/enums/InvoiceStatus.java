package com.example.invoicebackend.model.enums;

/**
 * Invoice workflow status values aligned with V1 migration.
 */
public enum InvoiceStatus {
    DRAFT,
    SENT,
    PAID,
    OVERDUE,
    CANCELED
}
