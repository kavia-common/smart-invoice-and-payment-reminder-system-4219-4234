package com.example.invoicebackend.service;

import com.example.invoicebackend.model.Invoice;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

/**
 * Service to render invoice HTML into PDF using OpenHTMLtoPDF.
 */
@Service
public class PdfService {

    // PUBLIC_INTERFACE
    public byte[] renderInvoice(Invoice invoice) {
        /** Renders a minimal Ocean Professional styled invoice PDF from entity. */
        String html = buildHtml(invoice);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    private String css() {
        // Ocean Professional style
        return "*{box-sizing:border-box} body{font-family:Inter,Arial,sans-serif;color:#111827;margin:0;padding:0;background:#f9fafb;}"
                + ".container{max-width:800px;margin:24px auto;background:#ffffff;border:1px solid #e5e7eb;border-radius:12px;overflow:hidden;box-shadow:0 10px 15px -3px rgba(0,0,0,0.1);}"
                + ".header{background:linear-gradient(90deg,#2563EB1A,#f9fafb);padding:24px;border-bottom:1px solid #e5e7eb;}"
                + ".title{font-size:24px;font-weight:700;color:#111827;margin:0}"
                + ".accent{color:#2563EB}"
                + ".meta{display:flex;justify-content:space-between;margin-top:12px;font-size:12px;color:#374151}"
                + ".section{padding:24px}"
                + ".grid{display:flex;gap:24px}"
                + ".card{flex:1;background:#F9FAFB;border:1px solid #E5E7EB;border-radius:10px;padding:16px}"
                + ".label{font-size:12px;color:#6B7280;text-transform:uppercase;letter-spacing:.04em;margin-bottom:4px}"
                + ".value{font-size:14px;color:#111827}"
                + "table{width:100%;border-collapse:collapse;margin-top:8px;border:1px solid #E5E7EB;border-radius:8px;overflow:hidden}"
                + "th,td{padding:10px;border-bottom:1px solid #E5E7EB;font-size:12px}"
                + "th{background:#F3F4F6;text-align:left;color:#111827}"
                + ".right{text-align:right}"
                + ".total{font-weight:700}"
                + ".footer{padding:16px;border-top:1px solid #e5e7eb;font-size:11px;color:#6B7280}";
    }

    private String buildHtml(Invoice inv) {
        StringBuilder rows = new StringBuilder();
        inv.getItems().forEach(item -> {
            rows.append("<tr>")
                .append("<td>").append(escape(item.getItemName())).append("</td>")
                .append("<td class='right'>").append(item.getQuantity()).append("</td>")
                .append("<td class='right'>").append(item.getUnitPrice()).append("</td>")
                .append("<td class='right'>").append(item.getLineTotal()).append("</td>")
                .append("</tr>");
        });

        String notes = inv.getNotes() != null ? escape(inv.getNotes()) : "";
        String customerName = inv.getCustomer() != null ? escape(inv.getCustomer().getName()) : "-";
        String partnerName = inv.getPartner() != null ? escape(inv.getPartner().getName()) : "-";
        String issue = inv.getIssueDate() != null ? inv.getIssueDate().toString() : "";
        String due = inv.getDueDate() != null ? inv.getDueDate().toString() : "";
        String status = inv.getStatus() != null ? inv.getStatus().name() : "-";

        StringBuilder sb = new StringBuilder();
        sb.append("<!doctype html><html><head><meta charset=\"utf-8\"><style>")
          .append(css())
          .append("</style></head><body>")
          .append("<div class=\"container\">")
          .append("<div class=\"header\">")
          .append("<h1 class=\"title\"><span class=\"accent\">Invoice</span> #")
          .append(escape(inv.getInvoiceNumber()))
          .append("</h1>")
          .append("<div class=\"meta\">")
          .append("<div>Partner: ").append(partnerName).append("</div>")
          .append("<div>Issue: ").append(issue).append("</div>")
          .append("<div>Due: ").append(due).append("</div>")
          .append("</div>")
          .append("</div>")
          .append("<div class=\"section grid\">")
          .append("<div class=\"card\"><div class=\"label\">Bill To</div><div class=\"value\">")
          .append(customerName).append("</div></div>")
          .append("<div class=\"card\"><div class=\"label\">Currency</div><div class=\"value\">")
          .append(escape(inv.getCurrency())).append("</div></div>")
          .append("<div class=\"card\"><div class=\"label\">Status</div><div class=\"value\">")
          .append(status).append("</div></div>")
          .append("</div>")
          .append("<div class=\"section\">")
          .append("<table><thead><tr>")
          .append("<th>Item</th><th class=\"right\">Qty</th><th class=\"right\">Unit</th><th class=\"right\">Line Total</th>")
          .append("</tr></thead><tbody>")
          .append(rows)
          .append("</tbody></table>")
          .append("<table style=\"margin-top:12px\"><tbody>")
          .append("<tr><td class=\"right\">Subtotal</td><td class=\"right\">").append(inv.getSubtotalAmount()).append("</td></tr>")
          .append("<tr><td class=\"right\">Tax</td><td class=\"right\">").append(inv.getTaxAmount()).append("</td></tr>")
          .append("<tr><td class=\"right\">Discount</td><td class=\"right\">").append(inv.getDiscountAmount()).append("</td></tr>")
          .append("<tr><td class=\"right total\">Total</td><td class=\"right total\">").append(inv.getTotalAmount()).append("</td></tr>")
          .append("</tbody></table>")
          .append("</div>")
          .append("<div class=\"section\"><div class=\"label\">Notes</div><div class=\"value\">")
          .append(notes).append("</div></div>")
          .append("<div class=\"footer\">Generated by Smart Invoice & Payment Reminder System</div>")
          .append("</div>")
          .append("</body></html>");
        return sb.toString();
    }

    private String escape(String input) {
        return input == null ? "" : input
                .replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
}
