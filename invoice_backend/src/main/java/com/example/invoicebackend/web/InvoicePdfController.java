package com.example.invoicebackend.web;

import com.example.invoicebackend.model.Invoice;
import com.example.invoicebackend.service.InvoiceService;
import com.example.invoicebackend.service.PdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Routes for generating Invoice PDFs on demand.
 */
@RestController
@RequestMapping("/api/invoices")
@Tag(name = "Invoices", description = "Invoice CRUD and search endpoints")
public class InvoicePdfController {

    private final InvoiceService invoices;
    private final PdfService pdfs;

    public InvoicePdfController(InvoiceService invoices, PdfService pdfs) {
        this.invoices = invoices;
        this.pdfs = pdfs;
    }

    // PUBLIC_INTERFACE
    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    @Operation(summary = "Generate invoice PDF", description = "Generates and streams a PDF for the specified invoice.")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        Invoice inv = invoices.find(id);
        byte[] bytes = pdfs.renderInvoice(inv);
        String filename = "invoice-" + inv.getInvoiceNumber() + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(bytes.length)
                .body(bytes);
    }
}
