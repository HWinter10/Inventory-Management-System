package com.hwinterton.inventory_api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hwinterton.inventory_api.dto.audit.AuditLogResponse;
import com.hwinterton.inventory_api.model.AuditLog;
import com.hwinterton.inventory_api.model.User;
import com.hwinterton.inventory_api.repository.AuditLogRepository;
import com.hwinterton.inventory_api.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for audit log business logic.
 *
 * <p>Handles recording important system actions and retrieving audit history.</p>
 */
@Slf4j // Lombok: logging feature helper, call replaced need for standard dependencies fields for Slf4j logging
@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogService(
            AuditLogRepository auditLogRepository,
            UserRepository userRepository
    ) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    /**
     * Method: records an audit log action.
     *
     * <p>The actor user is optional because some system actions may not be tied
     * to a logged-in user.</p>
     *
     * @param actorUserId the user ID that performed the action, or null for system actions
     * @param action the action that occurred
     * @param details additional details about the action
     * @return the saved audit log as a response DTO
     */
    @Transactional // protects workflow as a whole, as in it succeeds or fails as one unit
    public AuditLogResponse logAction(Long actorUserId, String action, String details) {
        log.info("Recording audit log action: {} for user id: {}", action, actorUserId);
                AuditLog auditLog = new AuditLog();

        if (actorUserId != null) {
            User actorUser = userRepository.findById(actorUserId)
                    .orElseThrow(() -> {
                        log.warn("Actor user not found with id: {}", actorUserId);
                        return new RuntimeException("User not found");
                    });
            auditLog.setActorUser(actorUser);
        }

        auditLog.setAction(action);
        auditLog.setDetails(details);

        AuditLog savedAuditLog = auditLogRepository.save(auditLog);
        log.info("Audit log recorded successfully with id: {}", savedAuditLog.getId());
        
        return toResponse(savedAuditLog);
    }

    /**
     * Method: retrieves paginated audit logs.
     *
     * @param pageable pagination information
     * @return paginated audit log response DTOs
     */
    @Transactional(readOnly = true) // protected workflow
    public Page<AuditLogResponse> getAuditLogs(Pageable pageable) {
        log.info("Fetching audit logs, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    // Converts AuditLog entity data into the response shape used by the frontend.
    private AuditLogResponse toResponse(AuditLog auditLog) {
        User actorUser = auditLog.getActorUser();

        Long actorUserId = actorUser != null ? actorUser.getId() : null;
        String actorUsername = actorUser != null ? actorUser.getUsername() : null;

        return new AuditLogResponse(
                auditLog.getId(),
                actorUserId,
                actorUsername,
                auditLog.getAction(),
                auditLog.getDetails(),
                auditLog.getCreatedAt()
        );
    }
}