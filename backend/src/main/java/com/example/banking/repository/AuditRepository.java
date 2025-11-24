package com.example.banking.repository;

import com.example.banking.domain.Audit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditRepository extends JpaRepository<Audit, Long> {
    List<Audit> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, String entityId);
}

