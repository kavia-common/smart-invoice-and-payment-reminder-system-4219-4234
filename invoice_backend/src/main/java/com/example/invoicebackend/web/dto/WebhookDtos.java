package com.example.invoicebackend.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * DTOs for incoming and outgoing webhooks.
 */
public class WebhookDtos {

    /**
     * Incoming webhook: invoice-created payload
     */
    public static class InvoiceCreatedWebhookRequest {
        @Schema(description = "Partner ID associated to the invoice", example = "1")
        @NotNull
        public Long partnerId;

        @Schema(description = "Customer ID for the invoice", example = "10")
        @NotNull
        public Long customerId;

        @Schema(description = "Invoice number, must be unique per partner", example = "INV-000123")
        @NotBlank
        public String invoiceNumber;

        @Schema(description = "Currency code", example = "USD")
        @NotBlank
        public String currency;

        @Schema(description = "Issue date")
        @NotNull
        public LocalDate issueDate;

        @Schema(description = "Optional due date")
        public LocalDate dueDate;

        @Schema(description = "Optional tax amount", example = "5.00")
        public BigDecimal taxAmount;

        @Schema(description = "Optional discount amount", example = "2.50")
        public BigDecimal discountAmount;

        @Schema(description = "Optional notes")
        public String notes;

        @Schema(description = "Arbitrary metadata for idempotency or correlation")
        public Map<String, Object> metadata;
    }

    /**
     * Incoming webhook: payment-updated payload
     */
    public static class PaymentUpdatedWebhookRequest {
        @Schema(description = "Partner ID", example = "1")
        @NotNull
        public Long partnerId;

        @Schema(description = "Invoice number to update", example = "INV-000123")
        @NotBlank
        public String invoiceNumber;

        @Schema(description = "Payment status", example = "PAID")
        @NotBlank
        public String paymentStatus;

        @Schema(description = "Optional payment reference / transaction id", example = "TXN-123")
        public String reference;

        @Schema(description = "Optional metadata for idempotency or correlation")
        public Map<String, Object> metadata;
    }

    /**
     * Generic webhook ack response
     */
    public static class WebhookAckResponse {
        public String status = "ok";
        public String message;
        public String requestId;

        public WebhookAckResponse() {}
        public WebhookAckResponse(String message, String requestId) {
            this.message = message;
            this.requestId = requestId;
        }
    }
}
