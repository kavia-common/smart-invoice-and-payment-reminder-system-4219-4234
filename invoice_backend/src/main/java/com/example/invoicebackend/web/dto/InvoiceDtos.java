package com.example.invoicebackend.web.dto;

import com.example.invoicebackend.model.enums.InvoiceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTOs for Invoice operations.
 */
public class InvoiceDtos {

    public static class InvoiceItemRequest {
        @NotBlank public String itemName;
        public String itemDescription;
        @NotNull public BigDecimal quantity;
        @NotNull public BigDecimal unitPrice;
    }

    public static class InvoiceCreateRequest {
        @NotNull public Long partnerId;
        @NotNull public Long customerId;
        public Long templateId;

        @NotBlank public String invoiceNumber;
        @Schema(example = "USD") @NotBlank public String currency;

        @NotNull public LocalDate issueDate;
        public LocalDate dueDate;

        public BigDecimal taxAmount = BigDecimal.ZERO;
        public BigDecimal discountAmount = BigDecimal.ZERO;
        public String notes;

        @Valid public List<InvoiceItemRequest> items;
    }

    public static class InvoiceUpdateRequest {
        public String invoiceNumber;
        public String currency;
        public LocalDate issueDate;
        public LocalDate dueDate;
        public InvoiceStatus status;
        public BigDecimal taxAmount;
        public BigDecimal discountAmount;
        public String notes;

        @Valid public List<InvoiceItemRequest> items;
    }

    public static class InvoiceResponse {
        public Long id;
        public String invoiceNumber;
        public String currency;
        public LocalDate issueDate;
        public LocalDate dueDate;
        public InvoiceStatus status;
        public BigDecimal subtotalAmount;
        public BigDecimal taxAmount;
        public BigDecimal discountAmount;
        public BigDecimal totalAmount;
    }
}
