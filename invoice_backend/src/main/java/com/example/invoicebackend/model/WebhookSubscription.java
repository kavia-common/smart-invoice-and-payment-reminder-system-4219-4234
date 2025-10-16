package com.example.invoicebackend.model;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Outbound webhook subscription for partner events.
 */
@Entity
@Table(name = "webhook_subscriptions")
public class WebhookSubscription {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "target_url", nullable = false, length = 1024)
    private String targetUrl;

    @Column(name = "secret_token")
    private String secretToken;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public WebhookSubscription() {}

    // getters and setters
    public Long getId() { return id; }
    public Partner getPartner() { return partner; }
    public void setPartner(Partner partner) { this.partner = partner; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }
    public String getSecretToken() { return secretToken; }
    public void setSecretToken(String secretToken) { this.secretToken = secretToken; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
