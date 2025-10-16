package com.example.invoicebackend.service;

import com.example.invoicebackend.model.Invoice;
import com.example.invoicebackend.model.Partner;
import com.example.invoicebackend.model.WebhookSubscription;
import com.example.invoicebackend.model.enums.InvoiceStatus;
import com.example.invoicebackend.repository.InvoiceRepository;
import com.example.invoicebackend.repository.PartnerRepository;
import com.example.invoicebackend.repository.WebhookSubscriptionRepository;
import com.example.invoicebackend.web.dto.InvoiceDtos;
import com.example.invoicebackend.web.dto.WebhookDtos.InvoiceCreatedWebhookRequest;
import com.example.invoicebackend.web.dto.WebhookDtos.PaymentUpdatedWebhookRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for handling incoming webhooks and publishing outgoing webhooks.
 */
@Service
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final InvoiceService invoiceService;
    private final InvoiceRepository invoiceRepository;
    private final PartnerRepository partnerRepository;
    private final WebhookSubscriptionRepository subscriptionRepository;

    @Value("${app.webhooks.outgoing.enabled:false}")
    private boolean outgoingEnabled;

    @Value("${app.webhooks.outgoing.defaultSecret:${WEBHOOK_OUTGOING_SIGNING_SECRET:}}")
    private String defaultSigningSecret;

    private final RestClient httpClient = RestClient.builder().build();

    public WebhookService(InvoiceService invoiceService,
                          InvoiceRepository invoiceRepository,
                          PartnerRepository partnerRepository,
                          WebhookSubscriptionRepository subscriptionRepository) {
        this.invoiceService = invoiceService;
        this.invoiceRepository = invoiceRepository;
        this.partnerRepository = partnerRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    /**
     * Verify signature placeholder. Currently returns true to accept all if no secret provided.
     * Replace with actual HMAC verification as needed.
     */
    public boolean verifyIncomingSignature(String signatureHeader, String payload, String secret) {
        if (secret == null || secret.isBlank()) {
            // No secret configured; accept for MVP but log
            log.warn("Incoming webhook signature verification skipped (no secret configured)");
            return true;
        }
        // Placeholder: naive md5; replace with HMAC SHA256 in production.
        String expected = DigestUtils.md5DigestAsHex((payload + secret).getBytes(StandardCharsets.UTF_8));
        boolean ok = expected.equalsIgnoreCase(signatureHeader);
        if (!ok) {
            log.warn("Incoming signature mismatch");
        }
        return ok;
    }

    // PUBLIC_INTERFACE
    @Transactional
    public Invoice handleInvoiceCreated(InvoiceCreatedWebhookRequest req) {
        // Map webhook request into existing InvoiceCreateRequest DTO for reuse
        InvoiceDtos.InvoiceCreateRequest create = new InvoiceDtos.InvoiceCreateRequest();
        create.partnerId = req.partnerId;
        create.customerId = req.customerId;
        create.invoiceNumber = req.invoiceNumber;
        create.currency = req.currency;
        create.issueDate = req.issueDate;
        create.dueDate = req.dueDate;
        create.taxAmount = req.taxAmount;
        create.discountAmount = req.discountAmount;
        create.notes = req.notes;
        // No items included in minimal webhook for MVP
        Invoice inv = invoiceService.create(create);
        return inv;
    }

    // PUBLIC_INTERFACE
    @Transactional
    public Invoice handlePaymentUpdated(PaymentUpdatedWebhookRequest req) {
        Partner partner = partnerRepository.findById(req.partnerId)
                .orElseThrow(() -> new IllegalArgumentException("Partner not found"));
        Optional<Invoice> invOpt = invoiceRepository.findByPartnerAndInvoiceNumber(partner, req.invoiceNumber);
        Invoice inv = invOpt.orElseThrow(() -> new IllegalArgumentException("Invoice not found for partner and number"));

        // Idempotent handling: if already PAID and status in payload indicates paid, do nothing
        String normalized = req.paymentStatus.trim().toUpperCase();
        if ("PAID".equals(normalized) && inv.getStatus() == InvoiceStatus.PAID) {
            return inv;
        }

        if ("PAID".equals(normalized)) {
            inv.setStatus(InvoiceStatus.PAID);
        } else if ("SENT".equals(normalized)) {
            inv.setStatus(InvoiceStatus.SENT);
        } else if ("OVERDUE".equals(normalized)) {
            inv.setStatus(InvoiceStatus.OVERDUE);
        } else {
            // Unknown mapping -> leave unchanged; still return invoice
            log.info("Unknown paymentStatus {}, leaving invoice status as {}", normalized, inv.getStatus());
        }
        return inv;
    }

    /**
     * Publish an outgoing webhook event when invoice status changes. Controlled via env toggle.
     */
    public void publishInvoiceStatusChange(Invoice invoice) {
        if (!outgoingEnabled) {
            return;
        }
        Partner partner = invoice.getPartner();
        List<WebhookSubscription> subs = subscriptionRepository.findByPartnerAndActiveTrue(partner);
        if (subs.isEmpty()) {
            return;
        }
        for (WebhookSubscription sub : subs) {
            try {
                sendEvent(sub, "invoice.status.changed", """
                        {"invoiceId": %d, "invoiceNumber": "%s", "status": "%s", "partnerId": %d}
                        """.formatted(invoice.getId(), invoice.getInvoiceNumber(), invoice.getStatus().name(), partner.getId()));
            } catch (Exception ex) {
                log.warn("Failed to publish webhook to {}: {}", sub.getTargetUrl(), ex.getMessage());
            }
        }
    }

    private void sendEvent(WebhookSubscription sub, String eventType, String jsonBody) {
        String secret = (sub.getSecretToken() != null && !sub.getSecretToken().isBlank()) ? sub.getSecretToken() : defaultSigningSecret;
        String signature = null;
        if (secret != null && !secret.isBlank()) {
            // Placeholder signature. Replace with HMAC in production
            signature = DigestUtils.md5DigestAsHex((jsonBody + secret).getBytes(StandardCharsets.UTF_8));
        }
        RestClient.RequestBodySpec req = httpClient.post()
                .uri(sub.getTargetUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Webhook-Event", eventType)
                .header("X-Request-Id", UUID.randomUUID().toString());
        if (signature != null) {
            req.header("X-Signature", signature);
        }
        req.body(jsonBody).retrieve().toBodilessEntity();
    }
}
