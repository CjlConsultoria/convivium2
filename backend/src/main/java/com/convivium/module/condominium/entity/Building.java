package com.convivium.module.condominium.entity;

import com.convivium.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "buildings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Building extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private Integer floors;

    @Column(name = "condominium_id", nullable = false)
    private Long condominiumId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condominium_id", insertable = false, updatable = false)
    private Condominium condominium;
}
