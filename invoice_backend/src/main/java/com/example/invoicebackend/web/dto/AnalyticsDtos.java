package com.example.invoicebackend.web.dto;

import com.example.invoicebackend.model.enums.InvoiceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTOs for Analytics endpoints responses and query params.
 */
public class AnalyticsDtos {

    /**
     * Summary KPI response for dashboard.
     */
    public static class SummaryResponse {
        @Schema(description = "Total outstanding amount (unpaid/overdue)", example = "15234.50")
        public BigDecimal totalOutstanding;

        @Schema(description = "Average payment delay in days for paid invoices within range", example = "4.2")
        public Double avgPaymentDelayDays;

        @Schema(description = "Rate of on-time payments among paid invoices within range, 0..1", example = "0.86")
        public Double onTimePaymentRate;

        @Schema(description = "Counts of invoices by status within range")
        public Map<InvoiceStatus, Long> statusCounts;
    }

    /**
     * Timeseries datapoint for charts.
     */
    public static class TimeseriesPoint {
        @Schema(description = "Month bucket date (first day of month)")
        public LocalDate period;

        @Schema(description = "Total amount for this period", example = "5000.00")
        public BigDecimal amount;

        @Schema(description = "Optional partner id grouped result", example = "1")
        public Long partnerId;

        @Schema(description = "Optional status dimension value")
        public InvoiceStatus status;
    }

    /**
     * Timeseries response wrapper.
     */
    public static class TimeseriesResponse {
        @Schema(description = "Series name or dimension like 'invoiced' or 'paid'")
        public String metric;

        @Schema(description = "Points ordered by period ascending")
        public List<TimeseriesPoint> points;
    }
}
