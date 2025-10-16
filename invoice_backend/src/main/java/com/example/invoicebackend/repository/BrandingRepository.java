package com.example.invoicebackend.repository;

import com.example.invoicebackend.model.Branding;
import com.example.invoicebackend.model.Partner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BrandingRepository extends JpaRepository<Branding, Long> {
    List<Branding> findByPartner(Partner partner);
}
