package com.example.invoicebackend.repository;

import com.example.invoicebackend.model.Customer;
import com.example.invoicebackend.model.Partner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByPartnerAndDeletedFalse(Partner partner);
}
