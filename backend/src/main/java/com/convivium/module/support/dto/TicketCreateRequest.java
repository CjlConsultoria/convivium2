package com.convivium.module.support.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TicketCreateRequest(
        @NotBlank @Size(max = 200) String subject
) {}
