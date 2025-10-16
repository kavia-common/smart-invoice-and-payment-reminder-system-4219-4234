package com.example.invoicebackend.web;

import com.example.invoicebackend.service.TemplateService;
import com.example.invoicebackend.web.dto.TemplateDtos.TemplateCreateRequest;
import com.example.invoicebackend.web.dto.TemplateDtos.TemplateResponse;
import com.example.invoicebackend.web.dto.TemplateDtos.TemplateUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing Templates.
 */
@RestController
@RequestMapping("/api/templates")
@Tag(name = "Templates", description = "Template CRUD endpoints")
public class TemplateController {

    private final TemplateService templates;

    public TemplateController(TemplateService templates) {
        this.templates = templates;
    }

    // PUBLIC_INTERFACE
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Create template", description = "Create a new template for a partner. Requires ADMIN or MANAGER.")
    public ResponseEntity<TemplateResponse> create(@Valid @RequestBody TemplateCreateRequest request) {
        return ResponseEntity.ok(templates.create(request));
    }

    // PUBLIC_INTERFACE
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    @Operation(summary = "Get template", description = "Get a template by id. Requires authentication.")
    public ResponseEntity<TemplateResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(templates.find(id));
    }

    // PUBLIC_INTERFACE
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    @Operation(summary = "List templates", description = "List templates for a partner with pagination.")
    public ResponseEntity<PageImpl<TemplateResponse>> listByPartner(
            @RequestParam Long partnerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<TemplateResponse> all = templates.listByPartner(partnerId);
        int from = Math.min(page * size, all.size());
        int to = Math.min(from + size, all.size());
        List<TemplateResponse> slice = all.subList(from, to);
        return ResponseEntity.ok(new PageImpl<>(slice, PageRequest.of(page, size), all.size()));
    }

    // PUBLIC_INTERFACE
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Update template", description = "Update template fields. Requires ADMIN or MANAGER.")
    public ResponseEntity<TemplateResponse> update(@PathVariable Long id, @Valid @RequestBody TemplateUpdateRequest request) {
        return ResponseEntity.ok(templates.update(id, request));
    }

    // PUBLIC_INTERFACE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Delete template", description = "Delete template. Requires ADMIN or MANAGER.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        templates.delete(id);
        return ResponseEntity.noContent().build();
    }
}
