package com.example.invoicebackend.service;

import com.example.invoicebackend.model.*;
import com.example.invoicebackend.model.enums.InvoiceStatus;
import com.example.invoicebackend.repository.*;
import com.example.invoicebackend.web.dto.InvoiceDtos.*;
import com.example.invoicebackend.web.mapper.DtoMappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Invoice service containing creation and update logic, soft-delete, and basic queries.
 */
@Service
public class InvoiceService {

    private final InvoiceRepository invoices;
    private final InvoiceItemRepository itemsRepo;
    private final PartnerRepository partners;
    private final CustomerRepository customers;
    private final TemplateRepository templates;

    public InvoiceService(InvoiceRepository invoices,
                          InvoiceItemRepository itemsRepo,
                          PartnerRepository partners,
                          CustomerRepository customers,
                          TemplateRepository templates) {
        this.invoices = invoices;
        this.itemsRepo = itemsRepo;
        this.partners = partners;
        this.customers = customers;
        this.templates = templates;
    }

    // PUBLIC_INTERFACE
    @Transactional
    public Invoice create(InvoiceCreateRequest req) {
        /** Create invoice with items, calculate totals, ensure uniqueness by partner + invoiceNumber. */
        Partner partner = partners.findById(req.partnerId).orElseThrow(() -> new IllegalArgumentException("Partner not found"));
        Customer customer = customers.findById(req.customerId).orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        Template template = null;
        if (req.templateId != null) {
            template = templates.findById(req.templateId).orElseThrow(() -> new IllegalArgumentException("Template not found"));
        }

        invoices.findByPartnerAndInvoiceNumber(partner, req.invoiceNumber).ifPresent(i -> {
            throw new IllegalArgumentException("Invoice number already exists for this partner");
        });

        Invoice inv = new Invoice();
        inv.setPartner(partner);
        inv.setCustomer(customer);
        inv.setTemplate(template);
        inv.setInvoiceNumber(req.invoiceNumber);
        inv.setCurrency(req.currency);
        inv.setIssueDate(req.issueDate);
        inv.setDueDate(req.dueDate);
        inv.setTaxAmount(req.taxAmount != null ? req.taxAmount : BigDecimal.ZERO);
        inv.setDiscountAmount(req.discountAmount != null ? req.discountAmount : BigDecimal.ZERO);
        inv.setNotes(req.notes);
        inv.setStatus(InvoiceStatus.DRAFT);

        List<InvoiceItem> items = new ArrayList<>();
        if (req.items != null) {
            for (InvoiceItemRequest ir : req.items) {
                InvoiceItem it = new InvoiceItem();
                it.setInvoice(inv);
                DtoMappers.applyInvoiceItemFromRequest(it, ir);
                items.add(it);
            }
        }
        inv.setItems(items);
        // Recompute subtotal and total
        inv.recalcTotals();
        DtoMappers.normalizeMoney(inv);

        return invoices.save(inv);
    }

    // PUBLIC_INTERFACE
    @Transactional
    public Invoice update(Long id, InvoiceUpdateRequest req) {
        /** Update invoice; if items provided, replace list. Recalc totals. */
        Invoice inv = invoices.findById(id).orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        // Basic field updates
        DtoMappers.applyInvoiceUpdateFromRequest(inv, req);
        DtoMappers.setDefaultInvoiceStatusIfMissing(inv);

        if (req.items != null) {
            // Replace items
            inv.getItems().clear();
            for (InvoiceItemRequest ir : req.items) {
                InvoiceItem it = new InvoiceItem();
                it.setInvoice(inv);
                DtoMappers.applyInvoiceItemFromRequest(it, ir);
                inv.getItems().add(it);
            }
        }

        // Recompute totals every update
        inv.recalcTotals();
        DtoMappers.normalizeMoney(inv);
        return inv;
    }

    // PUBLIC_INTERFACE
    @Transactional(readOnly = true)
    public Invoice find(Long id) {
        /** Find invoice by id. */
        return invoices.findById(id).orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
    }

    // PUBLIC_INTERFACE
    @Transactional(readOnly = true)
    public List<Invoice> listByPartner(Long partnerId) {
        /** List non-deleted invoices for a partner. */
        Partner partner = partners.findById(partnerId).orElseThrow(() -> new IllegalArgumentException("Partner not found"));
        return invoices.findByPartnerAndDeletedFalse(partner);
    }

    // PUBLIC_INTERFACE
    @Transactional
    public void softDelete(Long id) {
        /** Soft delete invoice (mark is_deleted = true). */
        Invoice inv = invoices.findById(id).orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        inv.setDeleted(true);
    }
}
