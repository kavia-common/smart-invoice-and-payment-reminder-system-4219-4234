package com.example.invoicebackend.web;

import com.example.invoicebackend.model.Invoice;
import com.example.invoicebackend.service.WebhookService;
import com.example.invoicebackend.web.dto.WebhookDtos.InvoiceCreatedWebhookRequest;
import com.example.invoicebackend.web.dto.WebhookDtos.PaymentUpdatedWebhookRequest;
import com.example.invoicebackend.web.dto.WebhookDtos.WebhookAckResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.UUID;

/**
 * Incoming webhook endpoints.
 */
@RestController
@RequestMapping("/api/webhooks")
@Tag(name = "Webhooks", description = "Incoming webhook endpoints for automation and payment updates")
public class WebhooksController {

    private final WebhookService webhookService;

    public WebhooksController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    // PUBLIC_INTERFACE
    @PostMapping(value = "/invoice-created", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Incoming: invoice-created",
            description = "Accepts a webhook payload to create/send an invoice via automation. Signature verification placeholder via X-Signature header.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Accepted", content = @Content(schema = @Schema(implementation = WebhookAckResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid payload")
            }
    )
    public ResponseEntity<WebhookAckResponse> invoiceCreated(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Invoice creation payload",
                    required = true,
                    content = @Content(schema = @Schema(implementation = InvoiceCreatedWebhookRequest.class))
            )
            @Valid @RequestBody InvoiceCreatedWebhookRequest payload,
            @Parameter(name = "X-Signature", description = "Signature header (optional)", required = false)
            @RequestHeader(value = "X-Signature", required = false) String signature,
            HttpServletRequest request
    ) throws IOException {
        String requestId = UUID.randomUUID().toString();
        String raw = readBody(request);
        // Placeholder verification with default secret; could be enhanced per-partner in future
        boolean ok = webhookService.verifyIncomingSignature(signature, raw, System.getenv("WEBHOOK_OUTGOING_SIGNING_SECRET"));
        if (!ok) {
            return ResponseEntity.status(401).body(err("invalid signature", requestId));
        }
        Invoice inv = webhookService.handleInvoiceCreated(payload);
        WebhookAckResponse resp = new WebhookAckResponse("created invoice " + inv.getInvoiceNumber(), requestId);
        return ResponseEntity.ok(resp);
    }

    // PUBLIC_INTERFACE
    @PostMapping(value = "/payment-updated", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Incoming: payment-updated",
            description = "Updates invoice/payment status; idempotent handling for repeated events. Signature verification placeholder via X-Signature header.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Accepted", content = @Content(schema = @Schema(implementation = WebhookAckResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid payload")
            }
    )
    public ResponseEntity<WebhookAckResponse> paymentUpdated(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Payment status update payload",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PaymentUpdatedWebhookRequest.class))
            )
            @Valid @RequestBody PaymentUpdatedWebhookRequest payload,
            @Parameter(name = "X-Signature", description = "Signature header (optional)", required = false)
            @RequestHeader(value = "X-Signature", required = false) String signature,
            HttpServletRequest request
    ) throws IOException {
        String requestId = UUID.randomUUID().toString();
        String raw = readBody(request);
        boolean ok = webhookService.verifyIncomingSignature(signature, raw, System.getenv("WEBHOOK_OUTGOING_SIGNING_SECRET"));
        if (!ok) {
            return ResponseEntity.status(401).body(err("invalid signature", requestId));
        }
        Invoice inv = webhookService.handlePaymentUpdated(payload);
        WebhookAckResponse resp = new WebhookAckResponse("updated invoice status to " + inv.getStatus(), requestId);
        return ResponseEntity.ok(resp);
    }

    private WebhookAckResponse err(String msg, String id) {
        WebhookAckResponse r = new WebhookAckResponse();
        r.status = "error";
        r.message = msg;
        r.requestId = id;
        return r;
    }

    private String readBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = request.getReader()) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }
}
