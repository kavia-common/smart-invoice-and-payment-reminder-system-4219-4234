package com.example.invoicebackend.repository;

import com.example.invoicebackend.model.Partner;
import com.example.invoicebackend.model.TaxSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaxSettingRepository extends JpaRepository<TaxSetting, Long> {
    List<TaxSetting> findByPartner(Partner partner);
}
