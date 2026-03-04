package com.convivium.module.support.dto;

import java.time.Instant;

public record MessageResponse(
        Long id,
        Long ticketId,
        Long senderId,
        String senderName,
        String message,
        Boolean fromAdmin,
        Boolean read,
        Instant createdAt
) {}
