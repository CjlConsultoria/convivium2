package com.convivium.module.condominium.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(name = "price_cents", nullable = false)
    private Integer priceCents;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "max_units", nullable = false)
    private Integer maxUnits;

    @Column(name = "max_users", nullable = false)
    private Integer maxUsers;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean active = true;
}
