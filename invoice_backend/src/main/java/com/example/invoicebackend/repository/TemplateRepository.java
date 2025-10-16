package com.example.invoicebackend.repository;

import com.example.invoicebackend.model.Partner;
import com.example.invoicebackend.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, Long> {
    List<Template> findByPartner(Partner partner);
}
