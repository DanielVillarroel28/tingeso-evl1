package com.example.tingeso_backend.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "fines")
@Data
public class FineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "loan_id", nullable = false)
    @JsonBackReference
    private LoanEntity loan;

    private String fineType; // "Atraso", "Daño irreparable"
    private int amount;
    private String status;

    private LocalDate creationDate;
    private LocalDate paymentDate;
}