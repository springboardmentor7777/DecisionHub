package com.decisionhub.service;

import com.decisionhub.dto.ApiDtos.AuditLogResponse;
import com.decisionhub.entity.AuditLog;
import com.decisionhub.repository.AuditLogRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    private final AuditLogRepository logs;

    public AuditService(AuditLogRepository logs) {
        this.logs = logs;
    }

    /** Joins the caller's transaction, so the log entry is saved only if the action itself succeeds. */
    @Transactional
    public void record(String actorEmail, String action, String targetType, Long targetId, String details) {
        String safeDetails = details != null && details.length() > 500 ? details.substring(0, 500) : details;
        logs.save(new AuditLog(actorEmail, action, targetType, targetId, safeDetails));
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> recent() {
        return logs.findTop100ByOrderByCreatedAtDesc().stream()
                .map(log -> new AuditLogResponse(log.getId(), log.getActorEmail(), log.getAction(),
                        log.getTargetType(), log.getTargetId(), log.getDetails(), log.getCreatedAt()))
                .toList();
    }
}
