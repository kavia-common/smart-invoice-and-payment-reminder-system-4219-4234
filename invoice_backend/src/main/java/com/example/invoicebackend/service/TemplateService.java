package com.example.invoicebackend.service;

import com.example.invoicebackend.model.Partner;
import com.example.invoicebackend.model.Template;
import com.example.invoicebackend.repository.PartnerRepository;
import com.example.invoicebackend.repository.TemplateRepository;
import com.example.invoicebackend.web.dto.TemplateDtos.TemplateCreateRequest;
import com.example.invoicebackend.web.dto.TemplateDtos.TemplateResponse;
import com.example.invoicebackend.web.dto.TemplateDtos.TemplateUpdateRequest;
import com.example.invoicebackend.web.mapper.DtoMappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Template CRUD logic.
 */
@Service
public class TemplateService {

    private final TemplateRepository templates;
    private final PartnerRepository partners;

    public TemplateService(TemplateRepository templates, PartnerRepository partners) {
        this.templates = templates;
        this.partners = partners;
    }

    // PUBLIC_INTERFACE
    @Transactional
    public TemplateResponse create(TemplateCreateRequest req) {
        /** Create a template for partner. */
        Partner partner = partners.findById(req.partnerId).orElseThrow(() -> new IllegalArgumentException("Partner not found"));
        Template t = new Template();
        t.setPartner(partner);
        t.setName(req.name);
        t.setTemplateType(req.templateType != null ? req.templateType : "INVOICE");
        t.setContentJson(req.contentJson);
        t.setDefault(req.isDefault);
        templates.save(t);
        return DtoMappers.toTemplateResponse(t);
    }

    // PUBLIC_INTERFACE
    @Transactional
    public TemplateResponse update(Long id, TemplateUpdateRequest req) {
        /** Update template fields. */
        Template t = templates.findById(id).orElseThrow(() -> new IllegalArgumentException("Template not found"));
        if (req.name != null) t.setName(req.name);
        if (req.templateType != null) t.setTemplateType(req.templateType);
        if (req.contentJson != null) t.setContentJson(req.contentJson);
        if (req.isDefault != null) t.setDefault(req.isDefault);
        return DtoMappers.toTemplateResponse(t);
    }

    // PUBLIC_INTERFACE
    @Transactional(readOnly = true)
    public TemplateResponse find(Long id) {
        /** Find template. */
        Template t = templates.findById(id).orElseThrow(() -> new IllegalArgumentException("Template not found"));
        return DtoMappers.toTemplateResponse(t);
    }

    // PUBLIC_INTERFACE
    @Transactional(readOnly = true)
    public List<TemplateResponse> listByPartner(Long partnerId) {
        /** List templates for partner. */
        Partner p = partners.findById(partnerId).orElseThrow(() -> new IllegalArgumentException("Partner not found"));
        return templates.findByPartner(p).stream().map(DtoMappers::toTemplateResponse).toList();
    }

    // PUBLIC_INTERFACE
    @Transactional
    public void delete(Long id) {
        /** Hard delete template (no soft delete column). */
        templates.deleteById(id);
    }
}
