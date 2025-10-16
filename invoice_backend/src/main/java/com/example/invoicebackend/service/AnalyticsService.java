package com.example.invoicebackend.service;

import com.example.invoicebackend.model.Invoice;
import com.example.invoicebackend.model.Partner;
import com.example.invoicebackend.model.Payment;
import com.example.invoicebackend.model.enums.InvoiceStatus;
import com.example.invoicebackend.model.enums.PaymentStatus;
import com.example.invoicebackend.repository.InvoiceRepository;
import com.example.invoicebackend.repository.PartnerRepository;
import com.example.invoicebackend.repository.PaymentRepository;
import com.example.invoicebackend.web.dto.AnalyticsDtos.SummaryResponse;
import com.example.invoicebackend.web.dto.AnalyticsDtos.TimeseriesPoint;
import com.example.invoicebackend.web.dto.AnalyticsDtos.TimeseriesResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service to compute analytics KPIs and time-series aggregations.
 */
@Service
public class AnalyticsService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PartnerRepository partnerRepository;

    public AnalyticsService(InvoiceRepository invoiceRepository,
                            PaymentRepository paymentRepository,
                            PartnerRepository partnerRepository) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.partnerRepository = partnerRepository;
    }

    // PUBLIC_INTERFACE
    @Transactional(readOnly = true)
    public SummaryResponse getSummary(Long partnerId,
                                      LocalDate from,
                                      LocalDate to,
                                      InvoiceStatus status) {
        /**
         * Compute KPIs for invoices within the optional date range and status filter.
         * Date filter applies to issueDate; for payments-based stats, uses paymentDate.
         */
        List<Invoice> source = listInvoices(partnerId);
        source = applyInvoiceFilters(source, from, to, status);

        // Total outstanding: sum totalAmount for invoices that are not PAID or CANCELED
        BigDecimal totalOutstanding = source.stream()
                .filter(i -> i.getStatus() != InvoiceStatus.PAID && i.getStatus() != InvoiceStatus.CANCELED)
                .map(Invoice::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Status counts
        Map<InvoiceStatus, Long> statusCounts = Arrays.stream(InvoiceStatus.values())
                .collect(Collectors.toMap(s -> s, s -> 0L, (a,b)->a, () -> new EnumMap<>(InvoiceStatus.class)));
        source.forEach(inv -> statusCounts.compute(inv.getStatus(), (k, v) -> v == null ? 1L : v + 1));

        // Average payment delay and on-time rate among PAID invoices
        // Join to payment repository to find completed payments per invoice
        List<Invoice> paidInvoices = source.stream()
                .filter(i -> i.getStatus() == InvoiceStatus.PAID)
                .collect(Collectors.toList());

        long onTimeCount = 0;
        long paidCountConsidered = 0;
        long totalDelayDays = 0;

        for (Invoice inv : paidInvoices) {
            List<Payment> payments = paymentRepository.findByInvoiceAndStatus(inv, PaymentStatus.COMPLETED);
            if (payments.isEmpty()) continue;

            // Use last payment date (assume full payment at latest date)
            LocalDate paidDate = payments.stream()
                    .map(Payment::getPaymentDate)
                    .filter(Objects::nonNull)
                    .max(LocalDate::compareTo)
                    .orElse(null);

            if (paidDate == null) continue;

            // If date range specified, consider payments that fall within range to compute KPIs
            if (from != null && paidDate.isBefore(from)) continue;
            if (to != null && paidDate.isAfter(to)) continue;

            LocalDate due = inv.getDueDate();
            if (due == null) continue;

            int delay = (int) (paidDate.toEpochDay() - due.toEpochDay());
            totalDelayDays += Math.max(0, delay);
            if (paidDate.isBefore(due) || paidDate.isEqual(due)) {
                onTimeCount++;
            }
            paidCountConsidered++;
        }

        Double avgDelay = paidCountConsidered == 0 ? 0.0 : (double) totalDelayDays / paidCountConsidered;
        Double onTimeRate = paidCountConsidered == 0 ? 0.0 : (double) onTimeCount / paidCountConsidered;

        SummaryResponse resp = new SummaryResponse();
        resp.totalOutstanding = totalOutstanding;
        resp.avgPaymentDelayDays = avgDelay;
        resp.onTimePaymentRate = onTimeRate;
        resp.statusCounts = statusCounts;
        return resp;
    }

    // PUBLIC_INTERFACE
    @Transactional(readOnly = true)
    public TimeseriesResponse getTimeseries(Long partnerId,
                                            LocalDate from,
                                            LocalDate to,
                                            InvoiceStatus status,
                                            String metric) {
        /**
         * Compute monthly timeseries for specified metric:
         * - "invoiced": sum invoice totalAmount by issueDate month
         * - "paid": sum payment amount by paymentDate month (completed only)
         * - default: "invoiced"
         */
        String m = metric == null ? "invoiced" : metric.toLowerCase(Locale.ROOT);
        if (!m.equals("paid") && !m.equals("invoiced")) {
            m = "invoiced";
        }

        List<TimeseriesPoint> points;
        if (m.equals("paid")) {
            points = buildPaidSeries(partnerId, from, to);
        } else {
            points = buildInvoicedSeries(partnerId, from, to, status);
        }

        // Ensure filled months between from and to with zero values
        if (from != null && to != null && !from.isAfter(to)) {
            points = fillMissingMonths(points, from, to);
        }

        TimeseriesResponse r = new TimeseriesResponse();
        r.metric = m;
        r.points = points.stream()
                .sorted(Comparator.comparing(p -> p.period))
                .toList();
        return r;
    }

    private List<Invoice> listInvoices(Long partnerId) {
        if (partnerId == null) {
            // All partners
            return invoiceRepository.findAll().stream()
                    .filter(i -> !i.isDeleted())
                    .toList();
        } else {
            Partner p = partnerRepository.findById(partnerId)
                    .orElseThrow(() -> new IllegalArgumentException("Partner not found"));
            return invoiceRepository.findByPartnerAndDeletedFalse(p);
        }
    }

    private List<Invoice> applyInvoiceFilters(List<Invoice> invoices,
                                              LocalDate from,
                                              LocalDate to,
                                              InvoiceStatus status) {
        return invoices.stream()
                .filter(i -> status == null || status == i.getStatus())
                .filter(i -> from == null || (i.getIssueDate() != null && !i.getIssueDate().isBefore(from)))
                .filter(i -> to == null || (i.getIssueDate() != null && !i.getIssueDate().isAfter(to)))
                .toList();
    }

    private List<TimeseriesPoint> buildInvoicedSeries(Long partnerId,
                                                      LocalDate from,
                                                      LocalDate to,
                                                      InvoiceStatus status) {
        List<Invoice> all = listInvoices(partnerId);
        all = applyInvoiceFilters(all, from, to, status);
        Map<YearMonth, BigDecimal> agg = new HashMap<>();
        for (Invoice inv : all) {
            LocalDate d = inv.getIssueDate();
            if (d == null) continue;
            YearMonth ym = YearMonth.from(d);
            BigDecimal amt = inv.getTotalAmount() == null ? BigDecimal.ZERO : inv.getTotalAmount();
            agg.merge(ym, amt, BigDecimal::add);
        }
        return agg.entrySet().stream().map(e -> {
            TimeseriesPoint p = new TimeseriesPoint();
            p.period = e.getKey().atDay(1);
            p.amount = e.getValue();
            return p;
        }).toList();
    }

    private List<TimeseriesPoint> buildPaidSeries(Long partnerId,
                                                  LocalDate from,
                                                  LocalDate to) {
        // For paid series we need to iterate invoices and their COMPLETED payments
        List<Invoice> base = listInvoices(partnerId);
        Map<YearMonth, BigDecimal> agg = new HashMap<>();
        for (Invoice inv : base) {
            List<Payment> payments = paymentRepository.findByInvoiceAndStatus(inv, PaymentStatus.COMPLETED);
            for (Payment pay : payments) {
                LocalDate d = pay.getPaymentDate();
                if (d == null) continue;
                if (from != null && d.isBefore(from)) continue;
                if (to != null && d.isAfter(to)) continue;
                YearMonth ym = YearMonth.from(d);
                BigDecimal amt = pay.getAmount() == null ? BigDecimal.ZERO : pay.getAmount();
                agg.merge(ym, amt, BigDecimal::add);
            }
        }
        return agg.entrySet().stream().map(e -> {
            TimeseriesPoint p = new TimeseriesPoint();
            p.period = e.getKey().atDay(1);
            p.amount = e.getValue();
            return p;
        }).toList();
    }

    private List<TimeseriesPoint> fillMissingMonths(List<TimeseriesPoint> points,
                                                    LocalDate from,
                                                    LocalDate to) {
        Map<LocalDate, TimeseriesPoint> byMonth = new HashMap<>();
        for (TimeseriesPoint p : points) {
            byMonth.put(p.period.withDayOfMonth(1), p);
        }
        List<TimeseriesPoint> filled = new ArrayList<>();
        YearMonth cur = YearMonth.from(from.withDayOfMonth(1));
        YearMonth end = YearMonth.from(to.withDayOfMonth(1));
        while (!cur.isAfter(end)) {
            LocalDate key = cur.atDay(1);
            TimeseriesPoint p = byMonth.get(key);
            if (p == null) {
                p = new TimeseriesPoint();
                p.period = key;
                p.amount = BigDecimal.ZERO;
            }
            filled.add(p);
            cur = cur.plusMonths(1);
        }
        return filled;
    }
}
