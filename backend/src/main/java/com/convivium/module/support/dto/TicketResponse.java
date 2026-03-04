package com.convivium.module.support.dto;

import java.time.Instant;

public record TicketResponse(
        Long id,
        String subject,
        String status,
        String userName,
        String condominiumName,
        String unitLabel,
        String roleName,
        Instant createdAt,
        Instant updatedAt,
        Long unreadCount
) {}
