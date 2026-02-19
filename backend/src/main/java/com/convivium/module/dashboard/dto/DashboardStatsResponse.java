package com.convivium.module.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    private long totalMoradores;
    private long denunciasAbertas;
    private long encomendasPendentes;
    private long reservasHoje;
}
