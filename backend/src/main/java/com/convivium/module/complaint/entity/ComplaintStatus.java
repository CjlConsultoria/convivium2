package com.convivium.module.complaint.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ComplaintStatus {

    OPEN("Aberta"),
    IN_REVIEW("Em Analise"),
    RESPONDED("Respondida"),
    RESOLVED("Resolvida"),
    CLOSED("Encerrada");

    private final String displayName;
}
