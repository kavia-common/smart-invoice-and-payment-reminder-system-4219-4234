package com.example.invoicebackend.web;

import com.example.invoicebackend.service.PartnerService;
import com.example.invoicebackend.web.dto.PartnerDtos.PartnerCreateRequest;
import com.example.invoicebackend.web.dto.PartnerDtos.PartnerResponse;
import com.example.invoicebackend.web.dto.PartnerDtos.PartnerUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing Partners.
 * Provides CRUD operations under /api/partners with basic pagination for list operation.
 */
@RestController
@RequestMapping("/api/partners")
@Tag(name = "Partners", description = "Partner management endpoints")
public class PartnerController {

    private final PartnerService partnerService;

    public PartnerController(PartnerService partnerService) {
        this.partnerService = partnerService;
    }

    // PUBLIC_INTERFACE
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(
            summary = "Create a partner",
            description = "Creates a new partner. Requires ADMIN or MANAGER role.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Partner created",
                            content = @Content(schema = @Schema(implementation = PartnerResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error")
            }
    )
    public ResponseEntity<PartnerResponse> create(@Valid @RequestBody PartnerCreateRequest request) {
        return ResponseEntity.ok(partnerService.create(request));
    }

    // PUBLIC_INTERFACE
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    @Operation(
            summary = "Get a partner by id",
            description = "Returns partner details. Requires authenticated user.",
            parameters = {
                    @Parameter(name = "id", in = ParameterIn.PATH, description = "Partner id")
            }
    )
    public ResponseEntity<PartnerResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(partnerService.find(id));
    }

    // PUBLIC_INTERFACE
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    @Operation(
            summary = "List partners by owner",
            description = "Returns a paginated list of partners for the provided ownerUserId. Requires authenticated user.",
            parameters = {
                    @Parameter(name = "ownerUserId", description = "Owner user id to filter partners"),
                    @Parameter(name = "page", description = "Page number starting at 0"),
                    @Parameter(name = "size", description = "Page size")
            }
    )
    public ResponseEntity<PageImpl<PartnerResponse>> listByOwner(
            @RequestParam Long ownerUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<PartnerResponse> all = partnerService.listByOwner(ownerUserId);
        int from = Math.min(page * size, all.size());
        int to = Math.min(from + size, all.size());
        List<PartnerResponse> slice = all.subList(from, to);
        return ResponseEntity.ok(new PageImpl<>(slice, PageRequest.of(page, size), all.size()));
    }

    // PUBLIC_INTERFACE
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(
            summary = "Update a partner",
            description = "Updates a partner's details. Requires ADMIN or MANAGER role."
    )
    public ResponseEntity<PartnerResponse> update(@PathVariable Long id, @Valid @RequestBody PartnerUpdateRequest request) {
        return ResponseEntity.ok(partnerService.update(id, request));
    }

    // PUBLIC_INTERFACE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(
            summary = "Delete a partner (soft-delete)",
            description = "Marks the partner as deleted. Requires ADMIN or MANAGER role."
    )
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        partnerService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
