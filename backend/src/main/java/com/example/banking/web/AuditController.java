package com.example.banking.web;

import com.example.banking.domain.Audit;
import com.example.banking.service.AuditService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audits")
public class AuditController {

    private final AuditService service;

    public AuditController(AuditService service) {
        this.service = service;
    }

    @GetMapping
    public List<Audit> list(@RequestParam String entityType, @RequestParam String entityId) {
        return service.findByEntity(entityType, entityId);
    }
}

