package com.example.invoicebackend.web;

import com.example.invoicebackend.model.Invoice;
import com.example.invoicebackend.model.enums.InvoiceStatus;
import com.example.invoicebackend.repository.InvoiceRepository;
import com.example.invoicebackend.repository.PartnerRepository;
import com.example.invoicebackend.service.InvoiceService;
import com.example.invoicebackend.web.dto.InvoiceDtos.InvoiceCreateRequest;
import com.example.invoicebackend.web.dto.InvoiceDtos.InvoiceResponse;
import com.example.invoicebackend.web.dto.InvoiceDtos.InvoiceUpdateRequest;
import com.example.invoicebackend.web.mapper.DtoMappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * REST controller for managing Invoices.
 */
@RestController
@RequestMapping("/api/invoices")
@Tag(name = "Invoices", description = "Invoice CRUD and search endpoints")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoiceRepository invoiceRepository;
    private final PartnerRepository partnerRepository;

    public InvoiceController(InvoiceService invoiceService,
                             InvoiceRepository invoiceRepository,
                             PartnerRepository partnerRepository) {
        this.invoiceService = invoiceService;
        this.invoiceRepository = invoiceRepository;
        this.partnerRepository = partnerRepository;
    }

    // PUBLIC_INTERFACE
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    @Operation(summary = "Create invoice", description = "Create a new invoice with items. Authenticated users can create.")
    public ResponseEntity<InvoiceResponse> create(@Valid @RequestBody InvoiceCreateRequest request) {
        Invoice created = invoiceService.create(request);
        return ResponseEntity.ok(DtoMappers.toInvoiceResponse(created));
    }

    // PUBLIC_INTERFACE
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    @Operation(summary = "Get invoice", description = "Get an invoice by id")
    public ResponseEntity<InvoiceResponse> get(@PathVariable Long id) {
        Invoice inv = invoiceService.find(id);
        return ResponseEntity.ok(DtoMappers.toInvoiceResponse(inv));
    }

    // PUBLIC_INTERFACE
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Update invoice", description = "Update invoice fields and items. Requires ADMIN or MANAGER.")
    public ResponseEntity<InvoiceResponse> update(@PathVariable Long id, @Valid @RequestBody InvoiceUpdateRequest request) {
        Invoice updated = invoiceService.update(id, request);
        return ResponseEntity.ok(DtoMappers.toInvoiceResponse(updated));
    }

    // PUBLIC_INTERFACE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Delete invoice (soft)", description = "Soft delete an invoice. Requires ADMIN or MANAGER.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        invoiceService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    // PUBLIC_INTERFACE
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    @Operation(summary = "List invoices by partner", description = "List invoices for a partner with optional filters and pagination.")
    public ResponseEntity<PageImpl<InvoiceResponse>> listByPartner(
            @RequestParam Long partnerId,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issueDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issueDateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // Base list by partner
        List<Invoice> all = invoiceService.listByPartner(partnerId);

        // Apply simple in-memory filters for MVP
        List<Invoice> filtered = all.stream()
                .filter(i -> status == null || status.equals(i.getStatus()))
                .filter(i -> customerId == null || (i.getCustomer() != null && Objects.equals(i.getCustomer().getId(), customerId)))
                .filter(i -> issueDateFrom == null || (i.getIssueDate() != null && !i.getIssueDate().isBefore(issueDateFrom)))
                .filter(i -> issueDateTo == null || (i.getIssueDate() != null && !i.getIssueDate().isAfter(issueDateTo)))
                .sorted(Comparator.comparing(Invoice::getIssueDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();

        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        List<InvoiceResponse> slice = filtered.subList(from, to).stream()
                .map(DtoMappers::toInvoiceResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new PageImpl<>(slice, PageRequest.of(page, size), filtered.size()));
    }

    // PUBLIC_INTERFACE
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    @Operation(summary = "Search invoices", description = "Search invoices by status or due date before for a partner.")
    public ResponseEntity<List<InvoiceResponse>> search(
            @RequestParam Long partnerId,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueBefore
    ) {
        var partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new IllegalArgumentException("Partner not found"));
        List<Invoice> result;
        if (status != null) {
            result = invoiceRepository.findByPartnerAndStatusAndDeletedFalse(partner, status);
        } else if (dueBefore != null) {
            result = invoiceRepository.findByPartnerAndDueDateBeforeAndDeletedFalse(partner, dueBefore);
        } else {
            result = invoiceRepository.findByPartnerAndDeletedFalse(partner);
        }
        return ResponseEntity.ok(result.stream().map(DtoMappers::toInvoiceResponse).toList());
    }

    // PUBLIC_INTERFACE
    @GetMapping("/generate-number")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    @Operation(summary = "Generate next invoice number", description = "Generates a simple next invoice number for a partner based on current count.")
    public ResponseEntity<String> generateNumber(@RequestParam Long partnerId) {
        // Simple MVP strategy: INV-<count+1> with zero padding. In future, move to dedicated service/sequence.
        var partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new IllegalArgumentException("Partner not found"));
        int count = invoiceRepository.findByPartnerAndDeletedFalse(partner).size();
        String next = String.format("INV-%06d", count + 1);
        return ResponseEntity.ok(next);
    }
}
