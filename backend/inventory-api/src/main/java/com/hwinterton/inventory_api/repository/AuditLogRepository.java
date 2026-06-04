package com.hwinterton.inventory_api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hwinterton.inventory_api.model.AuditLog;

/**
 * Repository for AuditLog database access.
 *
 * <p>Extends JpaRepository to inherit common CRUD methods and defines
 * lookup methods for audit log history.</p>
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Retrieves audit logs newest first for paginated audit history views.
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}