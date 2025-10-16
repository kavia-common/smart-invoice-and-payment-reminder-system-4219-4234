package com.example.invoicebackend.web;

import com.example.invoicebackend.model.enums.InvoiceStatus;
import com.example.invoicebackend.service.AnalyticsService;
import com.example.invoicebackend.web.dto.AnalyticsDtos.SummaryResponse;
import com.example.invoicebackend.web.dto.AnalyticsDtos.TimeseriesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Analytics endpoints providing KPI summary and timeseries for charts.
 */
@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "KPI and timeseries analytics endpoints")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // PUBLIC_INTERFACE
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    @Operation(
            summary = "Get analytics summary KPIs",
            description = "Returns KPI metrics like total outstanding, average payment delay, on-time rate, and status counts. " +
                    "Filters apply by issue date for invoice-based metrics and payment date for payment-based metrics.",
            parameters = {
                    @Parameter(name = "from", in = ParameterIn.QUERY, description = "Start date (inclusive)", example = "2024-01-01"),
                    @Parameter(name = "to", in = ParameterIn.QUERY, description = "End date (inclusive)", example = "2024-12-31"),
                    @Parameter(name = "partnerId", in = ParameterIn.QUERY, description = "Filter by partner id"),
                    @Parameter(name = "status", in = ParameterIn.QUERY, description = "Filter by invoice status")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Summary KPIs",
                            content = @Content(schema = @Schema(implementation = SummaryResponse.class)))
            }
    )
    public ResponseEntity<SummaryResponse> summary(
            @RequestParam(required = false) Long partnerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) InvoiceStatus status
    ) {
        SummaryResponse resp = analyticsService.getSummary(partnerId, from, to, status);
        return ResponseEntity.ok(resp);
    }

    // PUBLIC_INTERFACE
    @GetMapping("/timeseries")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    @Operation(
            summary = "Get analytics timeseries",
            description = "Returns monthly totals for the selected metric. Supported metrics: invoiced (default), paid. " +
                    "Filters apply by issue date for invoiced and payment date for paid.",
            parameters = {
                    @Parameter(name = "from", in = ParameterIn.QUERY, description = "Start date (inclusive)", example = "2024-01-01"),
                    @Parameter(name = "to", in = ParameterIn.QUERY, description = "End date (inclusive)", example = "2024-12-31"),
                    @Parameter(name = "partnerId", in = ParameterIn.QUERY, description = "Filter by partner id"),
                    @Parameter(name = "status", in = ParameterIn.QUERY, description = "Filter by invoice status (invoiced metric only)"),
                    @Parameter(name = "metric", in = ParameterIn.QUERY, description = "Metric to aggregate: invoiced|paid", example = "invoiced")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Timeseries response",
                            content = @Content(schema = @Schema(implementation = TimeseriesResponse.class)))
            }
    )
    public ResponseEntity<TimeseriesResponse> timeseries(
            @RequestParam(required = false) Long partnerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false, defaultValue = "invoiced") String metric
    ) {
        TimeseriesResponse resp = analyticsService.getTimeseries(partnerId, from, to, status, metric);
        return ResponseEntity.ok(resp);
    }
}
