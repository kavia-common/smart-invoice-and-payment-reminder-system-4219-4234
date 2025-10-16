package com.example.invoicebackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Line item of an invoice.
 */
@Entity
@Table(name = "invoice_items")
public class InvoiceItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @NotBlank
    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "item_description")
    private String itemDescription;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(name = "unit_price", precision = 19, scale = 4, nullable = false)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "line_total", precision = 19, scale = 2, nullable = false)
    private BigDecimal lineTotal = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public InvoiceItem() {}

    public void recalcLineTotal() {
        BigDecimal q = quantity != null ? quantity : BigDecimal.ZERO;
        BigDecimal up = unitPrice != null ? unitPrice : BigDecimal.ZERO;
        setLineTotal(up.multiply(q).setScale(2, BigDecimal.ROUND_HALF_UP));
    }

    // getters and setters
    public Long getId() { return id; }
    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getItemDescription() { return itemDescription; }
    public void setItemDescription(String itemDescription) { this.itemDescription = itemDescription; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
