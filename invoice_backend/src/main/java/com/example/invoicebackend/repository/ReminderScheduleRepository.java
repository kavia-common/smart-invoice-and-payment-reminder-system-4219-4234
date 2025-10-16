package com.example.invoicebackend.repository;

import com.example.invoicebackend.model.Partner;
import com.example.invoicebackend.model.ReminderSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReminderScheduleRepository extends JpaRepository<ReminderSchedule, Long> {
    List<ReminderSchedule> findByPartnerAndActiveTrue(Partner partner);
}
