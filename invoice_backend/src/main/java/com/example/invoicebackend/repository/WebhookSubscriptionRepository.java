package com.example.invoicebackend.repository;

import com.example.invoicebackend.model.Partner;
import com.example.invoicebackend.model.WebhookSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebhookSubscriptionRepository extends JpaRepository<WebhookSubscription, Long> {
    List<WebhookSubscription> findByPartnerAndActiveTrue(Partner partner);
}
