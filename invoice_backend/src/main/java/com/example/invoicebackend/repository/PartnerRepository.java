package com.example.invoicebackend.repository;

import com.example.invoicebackend.model.Partner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartnerRepository extends JpaRepository<Partner, Long> {
    List<Partner> findByOwnerUserIdAndDeletedFalse(Long ownerUserId);
}
