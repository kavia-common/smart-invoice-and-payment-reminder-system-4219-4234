package com.example.invoicebackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

/**
 * Automated reminder schedule definition.
 */
@Entity
@Table(name = "reminder_schedules")
public class ReminderSchedule {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String channel; // EMAIL, SMS, WHATSAPP

    @NotBlank
    @Column(name = "trigger_type", nullable = false)
    private String triggerType; // BEFORE_DUE, AFTER_DUE, CUSTOM

    @Column(name = "trigger_days", nullable = false)
    private Integer triggerDays = 0;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "template_id")
    private Template template;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public ReminderSchedule() {}

    // getters and setters
    public Long getId() { return id; }
    public Partner getPartner() { return partner; }
    public void setPartner(Partner partner) { this.partner = partner; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getTriggerType() { return triggerType; }
    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }
    public Integer getTriggerDays() { return triggerDays; }
    public void setTriggerDays(Integer triggerDays) { this.triggerDays = triggerDays; }
    public Template getTemplate() { return template; }
    public void setTemplate(Template template) { this.template = template; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
