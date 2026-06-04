package com.hwinterton.inventory_api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.hwinterton.inventory_api.dto.audit.AuditLogResponse;
import com.hwinterton.inventory_api.model.AuditLog;
import com.hwinterton.inventory_api.model.User;
import com.hwinterton.inventory_api.repository.AuditLogRepository;
import com.hwinterton.inventory_api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        auditLogService = new AuditLogService(auditLogRepository, userRepository);
    }

    // testing logAction creates an audit log with a user attached
    @Test
    void logAction_withActorUser_returnsAuditLogResponse() {
        User user = new User();
        user.setId(1L);
        user.setUsername("owner");

        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 4, 10, 30);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(auditLogRepository.save(any(AuditLog.class)))
                .thenAnswer(invocation -> {
                    AuditLog auditLog = invocation.getArgument(0);
                    auditLog.setId(100L);
                    auditLog.setCreatedAt(createdAt);
                    return auditLog;
                });

        AuditLogResponse response = auditLogService.logAction(
                1L,
                "CREATE_CATEGORY",
                "Created category Merchandise"
        );

        assertEquals(100L, response.id());
        assertEquals(1L, response.actorUserId());
        assertEquals("owner", response.actorUsername());
        assertEquals("CREATE_CATEGORY", response.action());
        assertEquals("Created category Merchandise", response.details());
        assertEquals(createdAt, response.createdAt());

        verify(userRepository).findById(1L);
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    // testing logAction can create a system audit log without a user
    @Test
    void logAction_withNullActorUser_returnsSystemAuditLogResponse() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 4, 11, 0);

        when(auditLogRepository.save(any(AuditLog.class)))
                .thenAnswer(invocation -> {
                    AuditLog auditLog = invocation.getArgument(0);
                    auditLog.setId(101L);
                    auditLog.setCreatedAt(createdAt);
                    return auditLog;
                });

        AuditLogResponse response = auditLogService.logAction(
                null,
                "SYSTEM_STARTUP",
                "System started"
        );

        assertEquals(101L, response.id());
        assertNull(response.actorUserId());
        assertNull(response.actorUsername());
        assertEquals("SYSTEM_STARTUP", response.action());
        assertEquals("System started", response.details());
        assertEquals(createdAt, response.createdAt());

        verify(userRepository, never()).findById(any());
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    // testing logAction throws when the actor user id does not exist
    @Test
    void logAction_whenActorUserNotFound_throwsRuntimeException() {
        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                auditLogService.logAction(
                        99L,
                        "CREATE_CATEGORY",
                        "Created category Merchandise"
                )
        );

        verify(userRepository).findById(99L);
        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    // testing getAuditLogs returns paginated AuditLogResponse data
    @Test
    void getAuditLogs_returnsPageOfAuditLogResponses() {
        User user = new User();
        user.setId(1L);
        user.setUsername("owner");

        AuditLog auditLog = new AuditLog();
        auditLog.setId(100L);
        auditLog.setActorUser(user);
        auditLog.setAction("CREATE_CATEGORY");
        auditLog.setDetails("Created category Merchandise");
        auditLog.setCreatedAt(LocalDateTime.of(2026, 6, 4, 10, 30));

        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditLog> auditLogPage = new PageImpl<>(List.of(auditLog), pageable, 1);

        when(auditLogRepository.findAllByOrderByCreatedAtDesc(pageable))
                .thenReturn(auditLogPage);

        Page<AuditLogResponse> response = auditLogService.getAuditLogs(pageable);

        assertEquals(1, response.getTotalElements());
        assertEquals(100L, response.getContent().get(0).id());
        assertEquals(1L, response.getContent().get(0).actorUserId());
        assertEquals("owner", response.getContent().get(0).actorUsername());
        assertEquals("CREATE_CATEGORY", response.getContent().get(0).action());

        verify(auditLogRepository).findAllByOrderByCreatedAtDesc(pageable);
    }

    // testing saved AuditLog contains the expected values before repository save
    @Test
    void logAction_savesExpectedAuditLogFields() {
        User user = new User();
        user.setId(1L);
        user.setUsername("owner");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(auditLogRepository.save(any(AuditLog.class)))
                .thenAnswer(invocation -> {
                    AuditLog auditLog = invocation.getArgument(0);
                    auditLog.setId(100L);
                    return auditLog;
                });

        auditLogService.logAction(
                1L,
                "UPDATE_PRODUCT",
                "Updated product Hoodie"
        );

        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedAuditLog = auditLogCaptor.getValue();

        assertEquals(user, savedAuditLog.getActorUser());
        assertEquals("UPDATE_PRODUCT", savedAuditLog.getAction());
        assertEquals("Updated product Hoodie", savedAuditLog.getDetails());
    }
}