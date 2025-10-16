package com.example.invoicebackend.web.mapper;

import com.example.invoicebackend.model.*;
import com.example.invoicebackend.model.enums.InvoiceStatus;
import com.example.invoicebackend.web.dto.InvoiceDtos.*;
import com.example.invoicebackend.web.dto.PartnerDtos.*;
import com.example.invoicebackend.web.dto.TemplateDtos.*;

import java.math.BigDecimal;

/**
 * Manual mappers for DTOs to entities and vice versa for MVP.
 */
public class DtoMappers {

    // PUBLIC_INTERFACE
    public static PartnerResponse toPartnerResponse(Partner p) {
        /** Map Partner entity to response DTO. */
        PartnerResponse r = new PartnerResponse();
        r.id = p.getId();
        r.name = p.getName();
        r.legalName = p.getLegalName();
        r.email = p.getEmail();
        r.phone = p.getPhone();
        r.city = p.getCity();
        r.country = p.getCountry();
        r.deleted = p.isDeleted();
        return r;
    }

    // PUBLIC_INTERFACE
    public static TemplateResponse toTemplateResponse(Template t) {
        /** Map Template entity to response DTO. */
        TemplateResponse r = new TemplateResponse();
        r.id = t.getId();
        r.partnerId = t.getPartner() != null ? t.getPartner().getId() : null;
        r.name = t.getName();
        r.templateType = t.getTemplateType();
        r.isDefault = t.isDefault();
        return r;
    }

    // PUBLIC_INTERFACE
    public static InvoiceResponse toInvoiceResponse(Invoice inv) {
        /** Map Invoice entity to response DTO. */
        InvoiceResponse r = new InvoiceResponse();
        r.id = inv.getId();
        r.invoiceNumber = inv.getInvoiceNumber();
        r.currency = inv.getCurrency();
        r.issueDate = inv.getIssueDate();
        r.dueDate = inv.getDueDate();
        r.status = inv.getStatus();
        r.subtotalAmount = inv.getSubtotalAmount();
        r.taxAmount = inv.getTaxAmount();
        r.discountAmount = inv.getDiscountAmount();
        r.totalAmount = inv.getTotalAmount();
        return r;
    }

    // PUBLIC_INTERFACE
    public static void applyInvoiceItemFromRequest(InvoiceItem item, InvoiceItemRequest req) {
        /** Update or set fields on InvoiceItem from request. */
        item.setItemName(req.itemName);
        item.setItemDescription(req.itemDescription);
        item.setQuantity(req.quantity);
        item.setUnitPrice(req.unitPrice);
        item.recalcLineTotal();
    }

    // PUBLIC_INTERFACE
    public static void applyInvoiceUpdateFromRequest(Invoice invoice, InvoiceUpdateRequest req) {
        /** Update basic invoice fields from update request (not partner/customer/template). */
        if (req.invoiceNumber != null) invoice.setInvoiceNumber(req.invoiceNumber);
        if (req.currency != null) invoice.setCurrency(req.currency);
        if (req.issueDate != null) invoice.setIssueDate(req.issueDate);
        if (req.dueDate != null) invoice.setDueDate(req.dueDate);
        if (req.status != null) invoice.setStatus(req.status);
        if (req.taxAmount != null) invoice.setTaxAmount(req.taxAmount);
        if (req.discountAmount != null) invoice.setDiscountAmount(req.discountAmount);
        if (req.notes != null) invoice.setNotes(req.notes);
    }

    // PUBLIC_INTERFACE
    public static void setDefaultInvoiceStatusIfMissing(Invoice invoice) {
        /** Ensure a default invoice status is set. */
        if (invoice.getStatus() == null) {
            invoice.setStatus(InvoiceStatus.DRAFT);
        }
    }

    // PUBLIC_INTERFACE
    public static void normalizeMoney(Invoice invoice) {
        /** Normalize null monetary fields to zero to avoid NPEs. */
        if (invoice.getSubtotalAmount() == null) invoice.setSubtotalAmount(BigDecimal.ZERO);
        if (invoice.getTaxAmount() == null) invoice.setTaxAmount(BigDecimal.ZERO);
        if (invoice.getDiscountAmount() == null) invoice.setDiscountAmount(BigDecimal.ZERO);
        if (invoice.getTotalAmount() == null) invoice.setTotalAmount(BigDecimal.ZERO);
    }
}
