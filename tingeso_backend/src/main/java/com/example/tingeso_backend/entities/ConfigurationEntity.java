package com.example.tingeso_backend.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "configurations")
@Data
public class ConfigurationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String configKey; // "daily_rental_fee", "daily_late_fee"

    private String configValue; // El valor de la tarifa
}