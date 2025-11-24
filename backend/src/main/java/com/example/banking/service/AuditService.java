package com.example.banking.service;

import com.example.banking.domain.Audit;
import com.example.banking.repository.AuditRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AuditService {

    private final AuditRepository repo;
    private final ObjectMapper mapper;

    public AuditService(AuditRepository repo, ObjectMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public Audit record(String actor, String action, String entityType, String entityId, Map<String, Object> details) {
        try {
            String d = details == null ? null : mapper.writeValueAsString(details);
            Audit a = Audit.builder()
                    .timestamp(OffsetDateTime.now())
                    .actor(actor)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(d)
                    .build();
            return repo.save(a);
        } catch (Exception e) {
            // Never fail the main flow because audit cannot be recorded. Log and continue.
            e.printStackTrace();
            return null;
        }
    }

    public List<Audit> findByEntity(String entityType, String entityId) {
        return repo.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
    }
}

