package com.example.invoicebackend.service;

import com.example.invoicebackend.model.Partner;
import com.example.invoicebackend.repository.PartnerRepository;
import com.example.invoicebackend.web.dto.PartnerDtos.PartnerCreateRequest;
import com.example.invoicebackend.web.dto.PartnerDtos.PartnerResponse;
import com.example.invoicebackend.web.dto.PartnerDtos.PartnerUpdateRequest;
import com.example.invoicebackend.web.mapper.DtoMappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Partner service offering basic CRUD with soft-delete.
 */
@Service
public class PartnerService {

    private final PartnerRepository partners;

    public PartnerService(PartnerRepository partners) {
        this.partners = partners;
    }

    // PUBLIC_INTERFACE
    @Transactional
    public PartnerResponse create(PartnerCreateRequest req) {
        /** Create a partner. */
        Partner p = new Partner();
        p.setOwnerUserId(req.ownerUserId);
        p.setName(req.name);
        p.setLegalName(req.legalName);
        p.setEmail(req.email);
        p.setPhone(req.phone);
        p.setAddressLine1(req.addressLine1);
        p.setAddressLine2(req.addressLine2);
        p.setCity(req.city);
        p.setState(req.state);
        p.setCountry(req.country);
        p.setPostalCode(req.postalCode);
        partners.save(p);
        return DtoMappers.toPartnerResponse(p);
    }

    // PUBLIC_INTERFACE
    @Transactional
    public PartnerResponse update(Long id, PartnerUpdateRequest req) {
        /** Update partner fields. */
        Partner p = partners.findById(id).orElseThrow(() -> new IllegalArgumentException("Partner not found"));
        if (req.name != null) p.setName(req.name);
        if (req.legalName != null) p.setLegalName(req.legalName);
        if (req.email != null) p.setEmail(req.email);
        if (req.phone != null) p.setPhone(req.phone);
        if (req.addressLine1 != null) p.setAddressLine1(req.addressLine1);
        if (req.addressLine2 != null) p.setAddressLine2(req.addressLine2);
        if (req.city != null) p.setCity(req.city);
        if (req.state != null) p.setState(req.state);
        if (req.country != null) p.setCountry(req.country);
        if (req.postalCode != null) p.setPostalCode(req.postalCode);
        return DtoMappers.toPartnerResponse(p);
    }

    // PUBLIC_INTERFACE
    @Transactional(readOnly = true)
    public PartnerResponse find(Long id) {
        /** Find partner by id. */
        Partner p = partners.findById(id).orElseThrow(() -> new IllegalArgumentException("Partner not found"));
        return DtoMappers.toPartnerResponse(p);
    }

    // PUBLIC_INTERFACE
    @Transactional(readOnly = true)
    public List<PartnerResponse> listByOwner(Long ownerUserId) {
        /** List all non-deleted partners for owner. */
        return partners.findByOwnerUserIdAndDeletedFalse(ownerUserId).stream()
                .map(DtoMappers::toPartnerResponse)
                .toList();
    }

    // PUBLIC_INTERFACE
    @Transactional
    public void softDelete(Long id) {
        /** Soft delete a partner. */
        Partner p = partners.findById(id).orElseThrow(() -> new IllegalArgumentException("Partner not found"));
        p.setDeleted(true);
    }
}
